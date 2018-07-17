package fs.playground.product

import com.fasterxml.jackson.databind.ObjectMapper
import fs.playground.core.*
import org.hibernate.StaleObjectStateException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.orm.jpa.JpaTransactionManager
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

enum class ProductEvent {
    CREATE, STOCK_QTY
}

@Service
class ProductService(
        @Autowired private val entityRepository: EntityRepository,
        @Autowired private val eventRepository: EventRepository,
        @Autowired private val jacksonObjectMapper: ObjectMapper,
        @Autowired private val trasactionManager: JpaTransactionManager
) {

    private fun toJson(product: Products) = jacksonObjectMapper.writeValueAsString(product)

    @Transactional
    fun create(productName: String, stockQty: Int): Events {
        val entity = entityRepository.save(Entities.create(this::class.java))
        val event = Events.create(
                entityId = entity.id.entityId,
                entityType = this::class.java,
                eventType = ProductEvent.CREATE,
                eventPayload = toJson(Products(productName, stockQty)))
        return eventRepository.save(event)
    }

    fun load(productId: Long): Products? {
        val foldFn: (Products?, Events) -> Products? = { product, event
            ->
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

    fun changeStockQty(productId: Long, stockQty: Int): Events? {
        val entity = entityRepository.getOne(EntityId(productId, this::class.java))
        return try {
            entity.updated = entity.updated.plusNanos(1)
            entityRepository.save(entity)
            val event = Events.create(
                    productId,
                    this::class.java,
                    ProductEvent.STOCK_QTY,
                    toJson(Products("[changeStockQty]", stockQty = stockQty)))
            eventRepository.save(event)
        } catch (e: Exception) {
            when (e) {
                is ObjectOptimisticLockingFailureException, is StaleObjectStateException -> {
                    println("locked ${stockQty}")
                    null
                }
                else -> {
                    throw e
                }
            }
        }
    }

}