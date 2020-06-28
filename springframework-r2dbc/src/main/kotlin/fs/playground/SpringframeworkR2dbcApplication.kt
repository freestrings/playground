package fs.playground

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.function.Consumer

@SpringBootApplication
class SpringframeworkR2dbcApplication

fun main(args: Array<String>) {
    val ctx = runApplication<SpringframeworkR2dbcApplication>(*args)
    val databaseClient = ctx.getBean(DatabaseClient::class.java)
    databaseClient.execute("CREATE TABLE person (name VARCHAR(255) PRIMARY KEY)")
            .fetch()
            .rowsUpdated()
            .and(databaseClient
                    .insert()
                    .into(Person::class.java)
                    .using(Person("test"))
                    .then()
            )
            .subscribe(Consumer { println("Done") })

}

@Configuration
class SpringframeworkR2dbcConfig : AbstractR2dbcConfiguration() {
    override fun connectionFactory(): ConnectionFactory {
//        return H2ConnectionFactory.inMemory("testdb")
        return ConnectionFactories.get("r2dbc:h2:mem:///testdb?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
    }

}

@Configuration
class Config {
    @Bean
    fun databaseClient(@Qualifier("connectionFactory") connectionFactory: ConnectionFactory) = DatabaseClient.create(connectionFactory)
}

@RestController
class Ctrl(val personRepository: PersonRepository) {

    @GetMapping("/get")
    suspend fun get(): Person {
        return personRepository.findByName("test")
    }
}

@Table
data class Person(
        @get:Id val name: String
)

@Repository
interface PersonRepository : CoroutineCrudRepository<Person, String> {
    suspend fun findByName(name: String): Person
}