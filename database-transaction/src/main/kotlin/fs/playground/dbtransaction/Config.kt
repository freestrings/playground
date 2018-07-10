package fs.playground.dbtransaction

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource


@Configuration
@EnableTransactionManagement
@EnableJpaRepositories
class JpaConfiguration(
        @Autowired private val env: Environment,
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