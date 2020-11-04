package fs.playground

import com.zaxxer.hikari.HikariDataSource
import fs.playground.LTCDispatcher.asAsync
import fs.playground.LTCDispatcher.asReadonlyTransaction
import fs.playground.entity.Person
import fs.playground.repository.PersonRepository
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Autowired
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
import org.springframework.transaction.support.DefaultTransactionStatus
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import java.util.concurrent.Executors
import javax.sql.DataSource
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class SpringframeworkWebfluxJpaApplication

fun main(args: Array<String>) {
    runApplication<SpringframeworkWebfluxJpaApplication>(*args)
}

@RestController
class Ctrl(val psersonService: PersonService) {

    @GetMapping("/complex/readonly")
    suspend fun complexReadOnlyWithWrite() {
        psersonService.complexWithReadOnly(UUID.randomUUID().toString())
    }

    @GetMapping("/complex")
    suspend fun complexWithWrite() {
        psersonService.complex(UUID.randomUUID().toString())
    }

    @GetMapping("/master/read")
    suspend fun masterRead(): Long {
        return psersonService.read("master - ${UUID.randomUUID()}")
    }

    @GetMapping("/master/write")
    suspend fun masterWrite() {
        psersonService.write(UUID.randomUUID().toString())
    }

    @GetMapping("/slave/read")
    suspend fun readFromSlave(): Long {
        return psersonService.readFromSlave("slave - ${UUID.randomUUID()}")
    }

    @GetMapping("/slave/write")
    suspend fun writeToSlave() {
        psersonService.writeToSlave(UUID.randomUUID().toString())
    }

    @GetMapping("/slave/write2")
    suspend fun writeToSlaveAsReadonl2() {
        asReadonlyTransaction { psersonService.write(UUID.randomUUID().toString()) }
    }

    @GetMapping("/slave/readall")
    suspend fun readAllFromSlave() {
        psersonService.readAllFromSlave("slaveall - ${UUID.randomUUID()}")
    }

    @GetMapping("/slave/read/async")
    suspend fun readFromSlaveAsyc(): Long {
        val r = psersonService.readFromSlaveAsync(UUID.randomUUID().toString())
        return r.await()
    }

}

@Service
class PersonService(val personRepository: PersonRepository) {

    @Autowired
    lateinit var _self: PersonService

    suspend fun read(uuid: String) = personRepository.countByName(uuid)

    suspend fun readAsync(uuid: String) = asAsync { personRepository.countByName(uuid) }

    suspend fun readFromSlave(uuid: String) = asReadonlyTransaction { read(uuid) }

    suspend fun readFromSlaveAsync(uuid: String) = asReadonlyTransaction { readAsync(uuid) }

    @Transactional
    suspend fun write(uuid: String) = personRepository.save(Person(name = uuid))

    @Transactional
    suspend fun writeToSlave(uuid: String) = asReadonlyTransaction { personRepository.save(Person(name = uuid)) }

    suspend fun readAllFromSlave(uuid: String): Long {
        return asReadonlyTransaction {
            val c1 = readAsync(uuid)
            val c2 = readAsync(uuid)
            val c3 = readFromSlaveAsync(uuid)
            val (r1, r2, r3) = awaitAll(c1, c2, c3)
            r1 + r2 + r3
        }
    }

    suspend fun complexWithReadOnly(uuid: String): Long {
        return asReadonlyTransaction {
            val c1 = readAsync(uuid)
            val c2 = readAsync(uuid)
            val c3 = readFromSlaveAsync(uuid)
            _self.write(uuid)
            val (r1, r2, r3) = awaitAll(c1, c2, c3)
            r1 + r2 + r3
        }
    }

    suspend fun complex(uuid: String): Long {
        val c1 = readAsync(uuid)
        val c2 = readAsync(uuid)
        val c3 = readFromSlaveAsync(uuid)
        _self.write(uuid)
        val (r1, r2, r3) = awaitAll(c1, c2, c3)
        return r1 + r2 + r3
    }
}

internal object LTC {
    private val dispatcher = Executors.newFixedThreadPool(2).asCoroutineDispatcher()
//    private val dispatcher = Dispatchers.Default

    enum class State {
        IN_READONLY,
    }

    private val localThread = ThreadLocal<State?>()

    fun get(): State? {
        return localThread.get()
    }

    fun set(value: State?) {
        localThread.set(value)
    }

    fun remove() {
        localThread.remove()
    }

    fun asContext(state: State? = null) = LTCContext(state)

    fun asCoroutineContext(state: State? = null): CoroutineContext {
        return if (state == null && get() == null) {
            dispatcher
        } else {
            asContext(state) + dispatcher
        }
    }
}

internal class LTCContext(state: LTC.State?) : ThreadContextElement<LTC.State?>, AbstractCoroutineContextElement(LTCContext) {
    companion object Key : CoroutineContext.Key<LTCContext>

    private val data = state?.let { it } ?: LTC.get()

    override fun updateThreadContext(context: CoroutineContext): LTC.State? {
        val old = LTC.get()
        LTC.set(data)
        return old
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: LTC.State?) {
        when (oldState) {
            LTC.State.IN_READONLY -> LTC.set(oldState)
            else -> LTC.remove()
        }
    }
}

object LTCDispatcher {

    fun <T> asAsync(call: () -> T): Deferred<T> = CoroutineScope(LTC.asCoroutineContext()).async {
        call()
    }

    suspend fun <T> asReadonlyTransaction(call: suspend () -> T): T {
        assert(LTC.get() != LTC.State.IN_READONLY)

        return withContext(LTC.asContext()) {
            val r = CoroutineScope(LTC.asCoroutineContext(LTC.State.IN_READONLY)).async {
                call()
            }
            r.await()
        }
    }

    fun isCurrentTransactionReadOnly() = LTC.get() == LTC.State.IN_READONLY
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
        val transactionManager = LTCJpaTransactionManager()
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
//            println("routing: ${LTCDispatcher.isCurrentTransactionReadOnly()} , ${TransactionSynchronizationManager.isCurrentTransactionReadOnly()}")
            return when {
                LTCDispatcher.isCurrentTransactionReadOnly() -> {
                    SLAVE_DB_KEY
                }
                TransactionSynchronizationManager.isCurrentTransactionReadOnly() -> {
                    SLAVE_DB_KEY
                }
                else -> {
                    MASTER_DB_KEY
                }
            }
        }
    }

    class LTCJpaTransactionManager : JpaTransactionManager() {
        override fun doCommit(status: DefaultTransactionStatus) {
//            println("commit: ${LTCDispatcher.isCurrentTransactionReadOnly()} , ${TransactionSynchronizationManager.isCurrentTransactionReadOnly()}")
            if (LTCDispatcher.isCurrentTransactionReadOnly()) {
                throw Exception("readonly transaction")
            } else {
                super.doCommit(status)
            }
        }
    }
}