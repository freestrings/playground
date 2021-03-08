package fs.playground

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

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
                println("jpaNative ${Thread.currentThread()}")
                jpaNative()
            }
            "jpql" -> {
                println("jpql ${Thread.currentThread()}")
                jpql()
            }
            "jpa" -> {
                println("jpa ${Thread.currentThread()}")
                jpa().toList()
            }
            else -> {
                println("jdbc ${Thread.currentThread()}")
                jdbc()
            }
        }
    }

    /**
     * 기본 워커에서 동작하고, 워커스레드 개수 확 늘어남
     */
    @GetMapping("/io")
    fun io(@RequestParam("type", required = false, defaultValue = "jdbc") type: String): List<Person> = query(type)


    @GetMapping("/loop")
    suspend fun deferredLoop(
        @RequestParam("count") count: Int,
        @RequestParam("type") type: String,
        @RequestParam("queryType", required = false, defaultValue = "jpa") queryType: String
    ): String {
        (0..count).map { io(queryType) }
        return "ok"
    }
}