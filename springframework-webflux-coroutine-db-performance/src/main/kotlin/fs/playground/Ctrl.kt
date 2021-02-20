package fs.playground

import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.asCoroutineDispatcher
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.scheduler.Schedulers
import java.util.*

@RestController
class Ctrl(val jdbcTemplate: JdbcTemplate, val personRepository: PersonRepository) {

    private fun jdbc(): List<Person> {
        val list = mutableListOf<Person>()
        jdbcTemplate.query("SELECT id, name FROM person") {
            list.add(Person(id = it.getLong("id"), name = it.getString("name")))
        }
        return list
    }

    private fun jpa() = personRepository.findAll()

    private fun jpaNative() = personRepository.findAllNative()

    private fun jpql() = personRepository.findAllJpql()

    /**
     * query performance
     *
     * ####
     * ==> JDBC > JQPL,JPA-Native > JPA
     * ####
     *
     * ## JPA
     * ab -c 100 -n 10000 http://localhost:8080/bounded\?type=jpa
     * Requests per second:    454.69 [#/sec] (mean)
     * Time per request:       219.932 [ms] (mean)
     * Time per request:       2.199 [ms] (mean, across all concurrent requests)
     * Transfer rate:          31.97 [Kbytes/sec] received
     *
     * Connection Times (ms)
     * min  mean[+/-sd] median   max
     * Connect:        0    0   0.3      0       7
     * Processing:    10  219  28.5    217     342
     * Waiting:        8  219  28.5    217     342
     * Total:         10  219  28.4    217     342
     *
     *
     * ## JPQL
     * ab -c 100 -n 10000 http://localhost:8080/bounded\?type=jpql
     * Requests per second:    1324.37 [#/sec] (mean)
     * Time per request:       75.508 [ms] (mean)
     * Time per request:       0.755 [ms] (mean, across all concurrent requests)
     * Transfer rate:          93.12 [Kbytes/sec] received
     *
     * Connection Times (ms)
     * min  mean[+/-sd] median   max
     * Connect:        0    1  10.3      0     132
     * Processing:     8   74  21.6     72     172
     * Waiting:        3   73  21.6     71     172
     * Total:          8   75  23.8     72     236
     *
     *
     * ## @Query(nativeQuery=true)
     * ab -c 100 -n 10000 http://localhost:8080/bounded\?type=jpaNative
     * Requests per second:    1283.63 [#/sec] (mean)
     * Time per request:       77.904 [ms] (mean)
     * Time per request:       0.779 [ms] (mean, across all concurrent requests)
     * Transfer rate:          90.26 [Kbytes/sec] received
     *
     * Connection Times (ms)
     * min  mean[+/-sd] median   max
     * Connect:        0    0   0.7      0      12
     * Processing:    11   77  26.3     72     240
     * Waiting:        3   77  26.2     72     240
     * Total:         11   77  26.2     73     240
     *
     *
     * ## JDBC
     * ab -c 100 -n 10000 http://localhost:8080/bounded\?type=jdbc
     * Requests per second:    1470.00 [#/sec] (mean)
     * Time per request:       68.027 [ms] (mean)
     * Time per request:       0.680 [ms] (mean, across all concurrent requests)
     * Transfer rate:          103.36 [Kbytes/sec] received
     *
     * Connection Times (ms)
     * min  mean[+/-sd] median   max
     * Connect:        0    0   0.5      0       7
     * Processing:     8   68  22.2     64     170
     * Waiting:        3   67  22.2     63     169
     * Total:          8   68  22.2     64     170
     */
    private fun query(type: String): List<Person> {
        return when (type) {
            "jpaNative" -> jpaNative()
            "jpql" -> jpql()
            "jpa" -> jpa().toList()
            else -> jdbc()
        }
    }

    /**
     * 기본 워커에서 동작하고, 워커스레드 개수 확 늘어남
     */
    @GetMapping("/io")
    suspend fun io(@RequestParam("type", required = false, defaultValue = "jdbc") type: String): List<Person> =
        coroutineScope {
            val a = async(Dispatchers.IO + TestCtx(UUID.randomUUID().toString())) {
                println("IO - ${Thread.currentThread().name}")
                query(type)
            }
            a.await()
        }

    /**
     * epoll 스레드에서 동작
     */
    @GetMapping("/default")
    suspend fun default(@RequestParam("type", required = false, defaultValue = "jdbc") type: String): List<Person> =
        coroutineScope {
            val a = async(TestCtx(UUID.randomUUID().toString())) {
                println("Default - ${Thread.currentThread().name}")
                query(type)
            }
            a.await()
        }

    /**
     * 기본 워커에서 동작하고
     * -Dkotlinx.coroutines.scheduler.core.pool.size 로 풀 크기가 조절됨
     */
    @GetMapping("/global")
    fun global(
        @RequestParam(
            "type",
            required = false,
            defaultValue = "jdbc"
        ) type: String
    ): Deferred<List<Person>> =
        GlobalScope.async(TestCtx(UUID.randomUUID().toString())) {
            println("Global - ${Thread.currentThread().name}")
            query(type)
        }

    val POOL = Schedulers.newBoundedElastic(10, 100000, "pool").asCoroutineDispatcher()

    /**
     * 지정한 풀에서 동작됨
     */
    @GetMapping("/bounded")
    suspend fun bounded(@RequestParam("type", required = false, defaultValue = "jdbc") type: String): List<Person> =
        coroutineScope {
            val a = async(POOL + TestCtx(UUID.randomUUID().toString())) {
                println("BOUNDED - ${Thread.currentThread().name}")
                query(type)
            }
            a.await()
        }

    /**
     * 지정한 풀에서 동작됨
     */
    @GetMapping("/bounded2")
    suspend fun bounded2(
        @RequestParam(
            "type",
            required = false,
            defaultValue = "jdbc"
        ) type: String
    ): List<Person> {
        val a = CoroutineScope(POOL + TestCtx(UUID.randomUUID().toString())).async {
            println("BOUNDED2 - ${Thread.currentThread().name}")
            query(type)
        }

        return a.await()
    }

    @GetMapping("/loop")
    suspend fun deferredLoop(
        @RequestParam("count") count: Int,
        @RequestParam("type") type: String,
        @RequestParam("type", required = false, defaultValue = "queryType") queryType: String
    ): String {
        when (type) {
            "io" -> (0..count).map { io(queryType) }
            "bounded" -> (0..count).map { bounded(queryType) }
            "bounded2" -> (0..count).map { bounded2(queryType) }
            "global" -> awaitAll(*(0..count).map { global(queryType) }.toTypedArray())
            else -> (0..count).map { default(queryType) }
        }

        return "ok"
    }
}