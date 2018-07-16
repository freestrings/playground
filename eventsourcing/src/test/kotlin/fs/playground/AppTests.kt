package fs.playground

import com.fasterxml.jackson.databind.ObjectMapper
import fs.playground.core.Entities
import fs.playground.core.EntityRepository
import fs.playground.core.EventRepository
import fs.playground.core.Events
import fs.playground.product.ProductService
import fs.playground.product.Products
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import javax.transaction.Transactional

@RunWith(SpringRunner::class)
@SpringBootTest
class AppTests {

    @Autowired
    lateinit var productService: ProductService

    @Autowired
    lateinit var entityRepository: EntityRepository

    @Autowired
    lateinit var eventRepository: EventRepository

    @Autowired
    lateinit var jacksonObjectMapper: ObjectMapper

    @Test
    @Transactional
    fun `프로덕트 생성`() {
        val productName = "testa"
        val stockQty = 10
        val event = productService.create(productName, stockQty)

        val foundEntity: Entities? = entityRepository.getOne(event.entityId)
        foundEntity?.let {
            assert(event.entityId.entityId == it.id.entityId)
        } ?: run {
            throw AssertionError()
        }

        val events = eventRepository.findAllByEntityId(event.entityId)
        if (events.isEmpty()) {
            throw AssertionError()
        }

        val targetEvent: Events = events.first()
        assert(event.entityId.entityId == targetEvent.entityId.entityId)
        assert(event.entityId.entityType == targetEvent.entityId.entityType)

        val foundProduct = jacksonObjectMapper.readValue(targetEvent.eventPayload, Products::class.java)
        assert(productName == foundProduct.productName)
        assert(stockQty == foundProduct.stockQty)
    }

    @Test
    fun `프로덕트 재고 변경`() {
        val productName = "testa"
        val stockQty = 10
        val event = productService.create(productName, stockQty)
        val productId = event.entityId.entityId

        productService.changeStockQty(productId, 1)
        assert(productService.load(productId)?.stockQty == 11)
        productService.changeStockQty(productId, 1)
        assert(productService.load(productId)?.stockQty == 12)
        productService.changeStockQty(productId, -1)
        assert(productService.load(productId)?.stockQty == 11)
    }
}
