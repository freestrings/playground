package fs.playground.product

import com.fasterxml.jackson.databind.ObjectMapper
import fs.playground.core.*
import org.hibernate.StaleObjectStateException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
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

interface SnapshotStrategy {
    fun canShapshot(events: List<Events>): Boolean
}

class DefaultSnapShotStrategy : SnapshotStrategy {
    override fun canShapshot(events: List<Events>) = events.size >= 10

}

interface ProductService {
    fun create(productName: String, productEntityPayload: ProductEntityPayload): Long?
    fun find(productId: Long): Products?
    fun adjustStockQty(productId: Long, stockQty: Int): AdustState
}

@Profile("pessimistic")
@Service
class ProductServicePessimisticLock(
        @Autowired val productRepository: ProductRepository
) : ProductService {
    override fun create(productName: String, productEntityPayload: ProductEntityPayload): Long? {
        val product = productRepository.save(Product(name = productName, stockQty = productEntityPayload.stockQty))
        return product.id
    }

    override fun find(productId: Long): Products? {
        val product = productRepository.findById(productId)
        return if (product.isPresent) {
            val p = product.get()
            Products(p.name, p.stockQty)
        } else {
            null
        }
    }

    @Transactional
    override fun adjustStockQty(productId: Long, stockQty: Int): AdustState {
        return try {
            productRepository.adjustStockQty(productId, stockQty)
            AdustState.ADJUSTED
        } catch (e: Exception) {
            AdustState.SOLDOUT
        }
    }
}

@Profile("optimistic")
@Service
class ProductServiceOptimisticLock(
        @Autowired private val entityRepository: EntityRepository,
        @Autowired private val eventRepository: EventRepository,
        @Autowired private val snapshotRepository: SnapshotRepository,
        @Autowired private val jacksonObjectMapper: ObjectMapper
) : ProductService {

    private val snapshotStrategy = DefaultSnapShotStrategy()

    private fun toJson(product: Products) = jacksonObjectMapper.writeValueAsString(product)
    private fun toJson(productEntityPayload: ProductEntityPayload) = jacksonObjectMapper.writeValueAsString(productEntityPayload)

    @Transactional
    override fun create(productName: String, productEntityPayload: ProductEntityPayload): Long {
        val entity = entityRepository.save(Entities.create(ProductService::class.java, toJson(productEntityPayload)))
        val event = Events.create(
                entityId = entity.entityId,
                entityType = ProductService::class.java,
                eventType = ProductEvent.CREATE,
                eventPayload = toJson(Products(productName, productEntityPayload.stockQty)))
        return eventRepository.save(event).entityId.entityId
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

        return eventRepository.findAllByEntityId(EntityId(productId, ProductService::class.java)).fold(null, foldFn)
    }

    private fun saveSnapshot(productId: Long, product: Products, events: List<Events>) {
        if (snapshotStrategy.canShapshot(events)) {
            val lastEvent = events.last()
            val create = Snapshots.create(lastEvent.eventId, productId, Products::class.java, toJson(product))
            try {
                snapshotRepository.save(create)
            } catch (e: Exception) {
            }
        }
    }

    override fun find(productId: Long): Products {
        val foldFn: (Products, Events) -> Products = { product, event ->
            val payload = jacksonObjectMapper.readValue(event.eventPayload, Products::class.java)
            when (ProductEvent.valueOf(event.eventType)) {
                ProductEvent.CREATE -> payload
                ProductEvent.STOCK_QTY -> product.apply(Products(product.productName, payload.stockQty))
            }
        }

        val snapshot = snapshotRepository.findTop1OrderByEventIdDesc(productId, Products::class.java)
        return snapshot?.let {
            val events = eventRepository.findAllByEventIdGreaterThanAndEntityIdOrderByEventIdAsc(it.eventId, EntityId(productId, ProductService::class.java))
            val product = events.fold(jacksonObjectMapper.readValue(snapshot.snapshotPayload, Products::class.java), foldFn)
            saveSnapshot(productId, product, events)
            product
        } ?: run {
            val events = eventRepository.findAllByEntityId(EntityId(productId, ProductService::class.java))
            val product = events.fold(Products("", 0), foldFn)
            saveSnapshot(productId, product, events)
            product
        }
    }

    private fun isSoldout(productId: Long, stockQty: Int): Boolean {
        return find(productId).stockQty + stockQty <= 0
    }

    /**
     * Transactional 이면 안된다. StaleObjectStateException에서 롤백된다.
     */
//    @Transactional
    override fun adjustStockQty(productId: Long, stockQty: Int): AdustState {
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
                val event = Events.create(productId, ProductService::class.java, ProductEvent.STOCK_QTY, toJson(Products("[adjustStockQty]", stockQty = stockQty)))
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

