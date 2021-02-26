package fs.playground

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions.parse
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories


@SpringBootApplication
class WebfluxCoroutineJdbcApplication

fun main(args: Array<String>) {
    runApplication<WebfluxCoroutineJdbcApplication>(*args)
}


//@Configuration
//@EnableR2dbcRepositories
//class DatabaseConfiguration : AbstractR2dbcConfiguration() {
//    override fun connectionFactory(): ConnectionFactory = ConnectionFactories.get(parse("r2dbc:pool:mysql://localhost:33060/testa?useUnicode=yes&characterEncoding=utf8&serverTimezone=Asia/Seoul"))
////            .get(builder()
////            .option(DRIVER, "pool")
////            .option(PROTOCOL, "mysql")
////            .option(HOST, "localhost")
////            .option(USER, "root")
////            .option(PORT, 33060)
////            .option(PASSWORD, "1234")
////            .option(DATABASE, "testa")
////            .option(MAX_SIZE, 64)
////            .option(INITIAL_SIZE, 32)
////            .build())
//}
