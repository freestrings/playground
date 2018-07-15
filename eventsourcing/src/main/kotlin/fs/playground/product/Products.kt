package fs.playground.product

import com.fasterxml.jackson.databind.ObjectMapper
import fs.playground.core.*
import org.springframework.beans.factory.annotation.Autowired
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
        @Autowired val entityRepository: EntityRepository,
        @Autowired val eventRepository: EventRepository,
        @Autowired val jacksonObjectMapper: ObjectMapper
) {

    @Transactional
    fun create(productName: String, stockQty: Int): Events {
        val entity = entityRepository.save(Entities.createEntity(this::class.java.name))
        return eventRepository.save(Events.createEvent(entityId = entity.id.entityId,
                entityType = entity.id.entityType,
                eventType = ProductEvent.CREATE.name,
                eventPayload = jacksonObjectMapper.writeValueAsString(Products(productName, stockQty))))
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

        return eventRepository.findAllByEntityId(EntityId(productId, this::class.java.name)).fold(null, foldFn)
    }

    fun changeStockQty(productId: Long, stockQty: Int): Events {
        return eventRepository.save(
                Events.createEvent(
                        productId,
                        this::class.java.name,
                        ProductEvent.STOCK_QTY.name,
                        jacksonObjectMapper.writeValueAsString(Products("[changeStockQty]", stockQty = stockQty))))
    }

}