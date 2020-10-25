package fs.playground

import com.zaxxer.hikari.HikariDataSource
import fs.playground.Dispatcher.asAsync
import fs.playground.Dispatcher.asReadonlyTransaction
import fs.playground.entity.Person
import fs.playground.repository.PersonRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.slf4j.MDCContext
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.JpaVendorAdapter
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.sql.DataSource

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class SpringframeworkWebfluxJpaApplication

fun main(args: Array<String>) {
    runApplication<SpringframeworkWebfluxJpaApplication>(*args)
}

@RestController
class Ctrl(val psersonService: PersonService) {

    @GetMapping("/master/read")
    suspend fun readFromMaster(): Long {
        return psersonService.readFromMaster(UUID.randomUUID().toString())
    }

    @GetMapping("/master/write")
    suspend fun writeToMaster() {
        psersonService.writeToMaster(UUID.randomUUID().toString())
    }

    @GetMapping("/master/write/asreadonly")
    suspend fun writeToMasterAsReadonly() {
        psersonService.writeToMasterAsReadonly(UUID.randomUUID().toString())
    }

    @GetMapping("/slave/read")
    suspend fun readFromSlave(): Long {
        return psersonService.readFromSlave(UUID.randomUUID().toString())
    }

    @GetMapping("/slave/write")
    suspend fun writeToSlaveAsReadonly() {
        psersonService.writeToSlaveAsReadonly(UUID.randomUUID().toString())
    }

    @GetMapping("/slave/readall")
    suspend fun readAllFromSlave() {
        psersonService.readAllFromSlave(UUID.randomUUID().toString())
    }

    @GetMapping("/slave/read/async")
    suspend fun readFromSlaveAsyc(): Long {
        return psersonService.readFromSlaveAsync(UUID.randomUUID().toString())
    }

}

@Service
class PersonService(val personRepository: PersonRepository) {

    suspend fun readFromMaster(uuid: String) = personRepository.countByName(uuid)

    @Transactional
    suspend fun writeToMaster(uuid: String) = personRepository.save(Person(name = uuid))

    suspend fun readAllFromSlave(uuid: String): Long {
        return asReadonlyTransaction {
            val c1 = asAsync { personRepository.countByName(uuid) }
            val c2 = asAsync { personRepository.countByName(uuid) }
            val c3 = asAsync { personRepository.countByName(uuid) }
            val (r1, r2, r3) = awaitAll(c1, c2, c3)
            r1 + r2 + r3
        }
    }

    /**
     * @Transactional 때문에 master에 write 된다.
     */
    @Transactional
    suspend fun writeToMasterAsReadonly(uuid: String) {
        return asReadonlyTransaction {
            personRepository.save(Person(name = uuid))
        }
    }

    /**
     * Exception 발생
     */
    suspend fun writeToSlaveAsReadonly(uuid: String) {
        return asReadonlyTransaction {
            writeToMaster(uuid)
        }
    }

    suspend fun readFromSlave(uuid: String): Long {
        return asReadonlyTransaction { personRepository.countByName(uuid) }
    }

    suspend fun readFromSlaveAsync(uuid: String): Long {
        return asReadonlyTransaction {
            val c1 = asAsync { personRepository.countByName(uuid) }
            c1.await()
        }
    }
}

/**
 * ThreadLocal 처리는 별도 구현하기 보다 안전하게 MDC 구현을 사용한다.
 */
object Dispatcher {
    private val KEY_READONLY = UUID.randomUUID().toString()

    fun <T> asAsync(call: () -> T): Deferred<T> = CoroutineScope(MDCContext() + Dispatchers.Default).async {
        call()
    }

    /**
     * 중첩된 asReadonlyTransaction 는 salve에 국한된 트랜잭션을 보장하지 않는다.
     *
     * 실제 쿼리 실행은 플래그 설정 이후 프로세스이기 때문
     * 예)
     * asReadonlyTransaction {
     *
     *  asReadonlyTransaction { // 1. 이 블럭 끝나면 플래그가 삭제된다.
     *      asAsync { .. }
     *  }
     *
     *  repository.count()
     *
     * }
     *
     * // 2. 실제 쿼리는 이 이후 실행 된다.
     */
    suspend fun <T> asReadonlyTransaction(call: suspend () -> T): T {
        return if (!isCurrentTransactionReadOnly()) {
            /**
             * 코로틴을 사용하는 코드에서 MDC에서 별도로 키를 remove 하려면 일반적으로 스레드가 다르기 때문에 주의가 필요하다.
             * withContext를 사용하면 안전하다.
             */
            withContext(MDCContext()) {
                MDC.put(KEY_READONLY, "evada")
                call()
            }
        } else {
            call()
        }
    }

    fun isCurrentTransactionReadOnly() = MDC.get(KEY_READONLY) != null
}

@Configuration
@EnableJpaRepositories(
        basePackages = ["fs.playground.repository"],
        enableDefaultTransactions = false
)
@EnableTransactionManagement
class Config {

    companion object {
        val SLAVE_DB_KEY = "slave"
        val MASTER_DB_KEY = "master"
    }

    @Bean(name = ["masterDataSource"])
    @ConfigurationProperties(prefix = "master.datasource")
    fun masterDataSource(): DataSource? {
        return DataSourceBuilder.create().type(HikariDataSource::class.java).build()
    }

    @Bean(name = ["slaveDataSource"])
    @ConfigurationProperties(prefix = "slave.datasource")
    fun slaveDataSource(): DataSource? {
        return DataSourceBuilder.create().type(HikariDataSource::class.java).build()
    }

    @Bean(name = ["routingDataSource"])
    fun routingDataSource(
            @Qualifier("masterDataSource") masterDataSource: DataSource,
            @Qualifier("slaveDataSource") slaveDataSource: DataSource,
    ): DataSource {
        val routingDataSource = ReplicationRoutingDataSource()
        val dataSourceMap: MutableMap<Any, Any> = HashMap()
        dataSourceMap[MASTER_DB_KEY] = masterDataSource
        dataSourceMap[SLAVE_DB_KEY] = slaveDataSource
        routingDataSource.setTargetDataSources(dataSourceMap)
        routingDataSource.setDefaultTargetDataSource(masterDataSource)
        return routingDataSource
    }

    @Primary
    @Bean
    fun dataSource(@Qualifier("routingDataSource") routingDataSource: DataSource): DataSource? {
        return LazyConnectionDataSourceProxy(routingDataSource)
    }

    @Bean
    fun entityManagerFactory(dataSource: DataSource): LocalContainerEntityManagerFactoryBean {
        val em = LocalContainerEntityManagerFactoryBean()
        em.dataSource = dataSource
        em.setPackagesToScan("fs.playground.entity")
        val vendorAdapter: JpaVendorAdapter = HibernateJpaVendorAdapter()
        em.jpaVendorAdapter = vendorAdapter
        em.setJpaProperties(additionalProperties())
        return em
    }

    @Bean
    fun transactionManager(dataSource: DataSource): PlatformTransactionManager? {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = entityManagerFactory(dataSource).getObject()
        return transactionManager
    }

    fun additionalProperties(): Properties {
        val properties = Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect")
        return properties;
    }

    class ReplicationRoutingDataSource : AbstractRoutingDataSource() {
        override fun determineCurrentLookupKey(): Any? {
            return if (Dispatcher.isCurrentTransactionReadOnly()) {
                SLAVE_DB_KEY
            } else if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
                SLAVE_DB_KEY
            } else {
                MASTER_DB_KEY
            }
        }
    }
}