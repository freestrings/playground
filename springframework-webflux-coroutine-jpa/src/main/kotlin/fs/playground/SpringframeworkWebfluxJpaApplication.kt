package fs.playground

import com.zaxxer.hikari.HikariDataSource
import fs.playground.entity.Person
import fs.playground.respository.PersonRepository
import kotlinx.coroutines.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.JpaVendorAdapter
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import java.util.concurrent.Executors
import javax.sql.DataSource


@SpringBootApplication
class SpringframeworkWebfluxJpaApplication

fun main(args: Array<String>) {
    runApplication<SpringframeworkWebfluxJpaApplication>(*args)
}

@RestController
class Ctrl(val psersonService: PersonService) {

    @GetMapping("/dispatcher")
    suspend fun get(): MutableIterable<Person> {
        return psersonService.getWithDispatcher(UUID.randomUUID().toString())
    }

    @GetMapping("/executor")
    suspend fun getWithExecutor(): MutableIterable<Person> {
        return psersonService.getWithExector(UUID.randomUUID().toString())
    }

    @GetMapping("/teste")
    suspend fun gete(): List<Person> {
        val uuid = UUID.randomUUID().toString()
        return psersonService.getAll2(uuid)
    }

    @GetMapping("/testi")
    suspend fun geti(): MutableIterable<Person> {
        val uuid = UUID.randomUUID().toString()
        val p1 = psersonService.getWithDispatcher(uuid)
        val p2 = psersonService.getWithDispatcher(uuid)
        p1.toMutableList().addAll(p2.toList())
        return p1
    }
}

@Service
class PersonService(val personRepository: PersonRepository, val dispatcher: ExecutorCoroutineDispatcher) {

    suspend fun getWithDispatcher(uuid: String): MutableIterable<Person> {
        return CoroutineScope(Dispatchers.Default).async {
            println("${uuid} - ${Thread.currentThread()}")
            personRepository.findAll()
        }.await()
    }

    suspend fun getWithExector(uuid: String): MutableIterable<Person> {
        return CoroutineScope(dispatcher).async {
            println("${uuid} - ${Thread.currentThread()}")
            personRepository.findAll()
        }.await()
    }

    suspend fun getAll2(uuid: String): List<Person> {
        return CoroutineScope(dispatcher).async {
            println("${uuid} - ${Thread.currentThread()}")
            personRepository.findAll() + personRepository.findAll()
        }.await()
    }
}

@Configuration
class Config {

    @Bean
    fun dataSource(): DataSource {
        return DataSourceBuilder.create()
                .type(HikariDataSource::class.java)
                .url("jdbc:mysql://localhost:33060/testdb?characterEncoding=UTF-8&serverTimezone=UTC")
                .username("root")
                .password("1234")
                .build()
    }

    @Bean
    fun entityManagerFactory(): LocalContainerEntityManagerFactoryBean {
        val em = LocalContainerEntityManagerFactoryBean()
        em.dataSource = dataSource()
        em.setPackagesToScan(*arrayOf("fs.playground.entity"))
        val vendorAdapter: JpaVendorAdapter = HibernateJpaVendorAdapter()
        em.jpaVendorAdapter = vendorAdapter
        em.setJpaProperties(additionalProperties())
        return em
    }

    @Bean
    fun transactionManager(): PlatformTransactionManager? {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = entityManagerFactory().getObject()
        return transactionManager
    }

    fun additionalProperties(): Properties {
        val properties = Properties();
//        properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
        return properties;
    }

    @Bean
    fun coroutineContext(): ExecutorCoroutineDispatcher {
        val threadPool = Executors.newFixedThreadPool(64)
        return threadPool.asCoroutineDispatcher()
    }
}