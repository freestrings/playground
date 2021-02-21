package fs.playground

import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.asCoroutineDispatcher
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional
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
     * ==> JDBC(tps ~=1,500) > JQPL,JPA-Native, JPA (tps ~= 1,300)
     * ####
     *
     *
     * !!! @EnableJpaRepositories(enableDefaultTransactions = false)
     * 설정 없으면 jpa 호출 때 불필요한 쿼리가 많이 생김
     *
     * ----------------
     * commit
     * autocommit=0
     * read-only/read-write transaction settings being sent
     * select ...
     * autocommit=1
     *
     * =>
     * select ...
     *
     * ----------------
     *
     * ## JPA
     * ab -c 100 -n 10000 http://localhost:8080/bounded\?type=jpa
     * ## JPQL
     * ab -c 100 -n 10000 http://localhost:8080/bounded\?type=jpql
     * ## @Query(nativeQuery=true)
     * ab -c 100 -n 10000 http://localhost:8080/bounded\?type=jpaNative
     * ## JDBC
     * ab -c 100 -n 10000 http://localhost:8080/bounded\?type=jdbc
     */
    private fun query(type: String): List<Person> {
        return when (type) {
            "jpaNative" -> {
                println("jpaNative")
                jpaNative()
            }
            "jpql" -> {
                println("jpql")
                jpql()
            }
            "jpa" -> {
                println("jpa")
                jpa().toList()
            }
            else -> {
                println("jdbc")
                jdbc()
            }
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
//    @Transactional(readOnly = true)
    suspend fun bounded(@RequestParam("type", required = false, defaultValue = "jdbc") type: String): List<Person> =
        coroutineScope {
            val a = async(POOL + TestCtx(UUID.randomUUID().toString())) {
                println("BOUNDED - $type - ${Thread.currentThread().name}")
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
        @RequestParam("queryType", required = false, defaultValue = "jpa") queryType: String
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