package fs.playground.dbtransaction

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(SpringRunner::class)
@SpringBootTest
class DbTransactionApplicationTests {

    @Autowired
    lateinit var testaService: TestaService

    @Autowired
    lateinit var repository: TestTableRepository

    @Test
    fun testWithoutTransaction() {
        var countLimit = 1000
        val id = "testb"
        repository.save(TestTable(id, 0, countLimit))

        var executor = Executors.newFixedThreadPool(5)
        for (i in 1..(countLimit + 10)) {
            executor.execute({ testaService.testWithoutTransaction(id) })
        }
        executor.shutdown()
        executor.awaitTermination(5, TimeUnit.SECONDS)
        println("Shutdown")

        val count = repository.getOne(id)?.let { it.count }
        println("Ineserted: ${count}/${countLimit}")
    }

    @Test
    fun testWithTransaction() {
        var countLimit = 1000
        val id = "testa"
        repository.save(TestTable(id, 0, countLimit))

        var executor = Executors.newFixedThreadPool(5)
        for (i in 1..(countLimit + 10)) {
            executor.execute({ testaService.testWithTransaction(id) })
        }
        executor.shutdown()
        executor.awaitTermination(5, TimeUnit.SECONDS)
        println("Shutdown")

        val count = repository.getOne(id)?.let { it.count }
        println("Ineserted: ${count}/${countLimit}")
    }

}
