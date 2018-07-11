package fs.playground.dbtransaction

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@RunWith(SpringRunner::class)
@SpringBootTest
class DbTransactionApplicationTests {

    @Autowired
    lateinit var testaService: TestaService

    @Autowired
    lateinit var repository: TestTableRepository

    private fun exec(id: String, runnable: (String) -> Unit) {
        var countLimit = 1000
        val rangeTo = countLimit + 10
        val countDownLatch = CountDownLatch(rangeTo)
        var executor = Executors.newFixedThreadPool(5)
        repository.save(TestTable(id, 0, countLimit))

        for (i in 1..rangeTo) {
            executor.execute({
                try {
                    runnable(id)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                countDownLatch.countDown()
            })
        }
        countDownLatch.await()
        println("Shutdown")

        val count = repository.getOne(id)?.let { it.count }
        println("Ineserted: ${count}/${countLimit}")
    }

    @Test
    fun testWithoutTransaction() {
        exec("testb") {
            testaService.testWithoutTransaction(it)
        }
    }

    @Test
    fun testWithTransaction() {
        exec("testa") {
            testaService.testWithTransaction(it)
        }
    }

}
