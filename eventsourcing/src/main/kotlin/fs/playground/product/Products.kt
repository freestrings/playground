package fs.playground.product

import com.fasterxml.jackson.databind.ObjectMapper
import fs.playground.core.*
import org.hibernate.StaleObjectStateException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Service
import java.lang.IllegalStateException
import javax.transaction.Transactional

data class Products(
        val productName: String,
        val stockQty: Int
) {
    fun apply(product: Products): Products {
        return Products(product.productName, this.stockQty + product.stockQty)
    }
}

data class ProductEntityPayload(
        val stockQty: Int,
        val soldOut: Boolean = false
)

enum class ProductEvent {
    CREATE, STOCK_QTY
}

enum class AdustState {
    ADJUSTED, RETRY, FAIL, SOLDOUT
}

@Service
class ProductService(
        @Autowired private val entityRepository: EntityRepository,
        @Autowired private val eventRepository: EventRepository,
        @Autowired private val snapshotRepository: SnapshotRepository,
        @Autowired private val jacksonObjectMapper: ObjectMapper
) {

    private fun toJson(product: Products) = jacksonObjectMapper.writeValueAsString(product)
    private fun toJson(productEntityPayload: ProductEntityPayload) = jacksonObjectMapper.writeValueAsString(productEntityPayload)

    @Transactional
    fun create(productName: String, productEntityPayload: ProductEntityPayload): Events {
        val entity = entityRepository.save(Entities.create(this::class.java, toJson(productEntityPayload)))
        val event = Events.create(
                entityId = entity.entityId,
                entityType = this::class.java,
                eventType = ProductEvent.CREATE,
                eventPayload = toJson(Products(productName, productEntityPayload.stockQty)))
        return eventRepository.save(event)
    }

    fun load(productId: Long): Products? {
        val foldFn: (Products?, Events) -> Products? = { product, event ->
            val payload = jacksonObjectMapper.readValue(event.eventPayload, Products::class.java)
            when (ProductEvent.valueOf(event.eventType)) {
                ProductEvent.CREATE -> payload
                ProductEvent.STOCK_QTY -> {
                    product?.let {
                        product.apply(Products(product.productName, payload.stockQty))
                    } ?: run {
                        throw IllegalStateException()
                    }
                }
            }
        }

        return eventRepository.findAllByEntityId(EntityId(productId, this::class.java)).fold(null, foldFn)
    }

    fun find(productId: Long): Products {
        val foldFn: (Products, Events) -> Products = { product, event ->
            val payload = jacksonObjectMapper.readValue(event.eventPayload, Products::class.java)
            when (ProductEvent.valueOf(event.eventType)) {
                ProductEvent.CREATE -> payload
                ProductEvent.STOCK_QTY -> product.apply(Products(product.productName, payload.stockQty))
            }
        }

        val snapshot = snapshotRepository.findTop1OrderByEventIdDesc()
        return snapshot?.let {
            val events = eventRepository.findAllByEventIdGreaterThanAndEntityIdOrderByEventIdAsc(it.eventId, EntityId(productId, this::class.java))
            val product = events.fold(jacksonObjectMapper.readValue(snapshot.snapshotPayload, Products::class.java), foldFn)
            if (!events.isEmpty()) {
                val lastEvent = events.last()
                val create = Snapshots.create(lastEvent.eventId, productId, Products::class.java, toJson(product))
                try {
                    snapshotRepository.save(create)
                } catch (e: Exception) {
                }
            }
            product
        } ?: run {
            val events = eventRepository.findAllByEntityId(EntityId(productId, this::class.java))
            val product = events.fold(Products("", 0), foldFn)
            if (!events.isEmpty()) {
                val lastEvent = events.last()
                val create = Snapshots.create(lastEvent.eventId, productId, Products::class.java, toJson(product))
                try {
                    snapshotRepository.save(create)
                } catch (e: Exception) {
                }
            }
            product
        }
    }

    private fun isSoldout(productId: Long, stockQty: Int): Boolean {
        return find(productId).stockQty + stockQty <= 0
//            return load(productId)?.let { it.stockQty + stockQty } ?: run { stockQty }
    }

    fun adjustStockQty(productId: Long, stockQty: Int): AdustState {
        val entity = entityRepository.getOne(productId)
        val entityPayload = jacksonObjectMapper.readValue(entity.entityPayload, ProductEntityPayload::class.java)
        if (entityPayload.soldOut) {
            return AdustState.SOLDOUT
        }
        return try {
            entity.updated = entity.updated.plusNanos(1)
            var newEntity = entityRepository.save(entity)
            if (isSoldout(productId, stockQty)) {
                newEntity.entityPayload = toJson(ProductEntityPayload(entityPayload.stockQty, true))
                entityRepository.save(newEntity)
                AdustState.SOLDOUT
            } else {
                val event = Events.create(productId, this::class.java, ProductEvent.STOCK_QTY, toJson(Products("[adjustStockQty]", stockQty = stockQty)))
                eventRepository.save(event)
                AdustState.ADJUSTED
            }
        } catch (e: Exception) {
            when (e) {
                is ObjectOptimisticLockingFailureException, is StaleObjectStateException -> {
                    AdustState.RETRY
                }
                else -> {
                    AdustState.FAIL
                }
            }
        }
    }

}