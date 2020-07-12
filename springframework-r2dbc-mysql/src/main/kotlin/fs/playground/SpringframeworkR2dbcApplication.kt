package fs.playground

import io.r2dbc.pool.PoolingConnectionFactoryProvider
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions.*
import io.r2dbc.spi.ValidationDepth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@SpringBootApplication
class SpringframeworkR2dbcApplication

var logger: Log = LogFactory.getLog(SpringframeworkR2dbcApplication::class.java)

fun main(args: Array<String>) {
    runApplication<SpringframeworkR2dbcApplication>(*args)
}

@RestController
class Ctrl(val databaseClient: DatabaseClient) {

    @GetMapping("/testa")
    fun get(): Mono<MutableList<Person>> {
        return get(databaseClient)
    }

    @GetMapping("/teste")
    fun getFlux(): Flux<Person> {
        return getFlux(databaseClient)
    }
}

@Configuration
class Config {

    @Bean
    fun connectionFactory(): ConnectionFactory {
        val options = builder()
                .option(DRIVER, "pool")
                .option(PROTOCOL, "mysql")
                .option(HOST, "127.0.0.1")
                .option(USER, "root")
                .option(PORT, 33060)  // optional, default 3306
                .option(PASSWORD, "1234") // optional, default null, null means has no password
                .option(DATABASE, "testdb")
                .option(PoolingConnectionFactoryProvider.INITIAL_SIZE, 8)
                .option(PoolingConnectionFactoryProvider.MAX_SIZE, 1024)
                .option(PoolingConnectionFactoryProvider.VALIDATION_DEPTH, ValidationDepth.LOCAL)
                .option(PoolingConnectionFactoryProvider.VALIDATION_QUERY, "select 1")
                .build()

        return ConnectionFactories.get(options)
    }

    @Bean
    fun databaseClient(@Qualifier("connectionFactory") connectionFactory: ConnectionFactory): DatabaseClient {
        return DatabaseClient.create(connectionFactory)
    }
}

fun get(databaseClient: DatabaseClient): Mono<MutableList<Person>> {
    return databaseClient.execute("select * from person /*${Thread.currentThread()}*/")
            .map { r ->
                Person(r.get("id", Long::class.java), r.get("name", String::class.java))
            }
            .all()
            .collectList()
}

fun getFlux(databaseClient: DatabaseClient): Flux<Person> {
    return databaseClient.execute("select * from person /*${Thread.currentThread()}*/")
            .map { r ->
                Person(r.get("id", Long::class.java), r.get("name", String::class.java))
            }
            .all()
}

data class Person(var id: Long?, var name: String?)