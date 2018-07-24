package fs.playground

import com.fasterxml.jackson.databind.ObjectMapper
import fs.playground.core.*
import fs.playground.product.*
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@RunWith(SpringRunner::class)
@SpringBootTest
class AppTests {

    @Autowired
    lateinit var productService: ProductServiceOptimisticLock

    @Autowired
    lateinit var entityRepository: EntityRepository

    @Autowired
    lateinit var eventRepository: EventRepository

    @Autowired
    lateinit var jacksonObjectMapper: ObjectMapper

    @Test
    fun `프로덕트 생성`() {
        val productName = "testa"
        val stockQty = 10
        val entityId = productService.create(productName, ProductEntityPayload(stockQty))

        val foundEntity: Entities? = entityRepository.getOne(entityId)
        foundEntity?.let {
            assert(entityId == it.entityId)
        } ?: run {
            throw AssertionError()
        }

        val events = eventRepository.findAllByEntityId(EntityId(entityId, ProductService::class.java))
        if (events.isEmpty()) {
            throw AssertionError()
        }

        val targetEvent: Events = events.first()
        assert(entityId == targetEvent.entityId.entityId)

        val foundProduct = jacksonObjectMapper.readValue(targetEvent.eventPayload, Products::class.java)
        assert(productName == foundProduct.productName)
        assert(stockQty == foundProduct.stockQty)
    }

    @Test
    fun `프로덕트 재고 변경`() {
        val productName = "testa"
        val stockQty = 10
        val productId = productService.create(productName, ProductEntityPayload(stockQty))

        productService.adjustStockQty(productId, 1)
        assert(productService.load(productId)?.stockQty == 11)
        productService.adjustStockQty(productId, 1)
        assert(productService.load(productId)?.stockQty == 12)
        productService.adjustStockQty(productId, -1)
        assert(productService.load(productId)?.stockQty == 11)
    }

    @Test
    fun `프로덕트 재고 변경 락`() {
        val buy: (Long) -> Boolean = { productId ->
            var done = false
            val iter = 10
            val countDownLatch = CountDownLatch(iter)
            var executor = Executors.newFixedThreadPool(5)
            for (i in 1..iter) {
                executor.execute {
                    val adustState = productService.adjustStockQty(productId, -1)
                    countDownLatch.countDown()
                    when (adustState) {
                        AdustState.FAIL -> {
                            done = true
                            assert(false)
                        }
                        AdustState.SOLDOUT -> {
                            done = true
                            println("soldout")
                        }
                        else -> {
                            // ignored
                        }
                    }
                }
            }

            while (countDownLatch.count > 0) {
                Thread.sleep(10)
            }

            executor.shutdown()

            done
        }

        val productId = productService.create("testa", ProductEntityPayload(100))

        while (!buy(productId)) {
            Thread.sleep(10)
        }
    }
}
