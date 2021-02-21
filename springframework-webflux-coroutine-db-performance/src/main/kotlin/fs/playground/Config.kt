package fs.playground

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableJpaRepositories(
    basePackages = ["fs.playground"],
    enableDefaultTransactions = false
)
@EnableTransactionManagement
class Config {
}