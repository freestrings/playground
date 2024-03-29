package fs.playground

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
class JpaConfiguration(
        @Autowired private val dataSource: DataSource
) {

    @Bean
    fun entityManagerFactory(): LocalContainerEntityManagerFactoryBean {
        val hibernateJpa = HibernateJpaVendorAdapter()
        val emf = LocalContainerEntityManagerFactoryBean()
        emf.dataSource = dataSource
        emf.setPackagesToScan(JpaConfiguration::class.java.getPackage().getName())
        emf.jpaVendorAdapter = hibernateJpa
        return emf
    }

    @Bean
    fun transactionManager(): JpaTransactionManager {
        val tx = JpaTransactionManager()
        tx.entityManagerFactory = entityManagerFactory().`object`
        return tx
    }
}

@Configuration
class PayloadConfiguration {

    @Bean
    fun jacksonObjectMapper(): ObjectMapper {
        return com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
    }
}