package fs.playground.r2dbc

import io.r2dbc.pool.PoolingConnectionFactoryProvider.*
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.ConnectionFactoryOptions.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories


@Configuration
@EnableR2dbcRepositories(
    basePackages = ["fs.playground.r2dbc"],
)
class R2dbcConfig : AbstractR2dbcConfiguration() {

    @Bean
    override fun connectionFactory(): ConnectionFactory {
        return ConnectionFactories.get(
            ConnectionFactoryOptions.builder()
                .option(DRIVER, "pool")
                .option(PROTOCOL, "mysql")
                .option(HOST, "192.168.25.43")
                .option(USER, "root")
                .option(PORT, 33060)
                .option(PASSWORD, "1234")
                .option(DATABASE, "testa")
                .option(MAX_SIZE, 10)
                .build()
        )
    }

}