package fs.playground.jpa

import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.JpaVendorAdapter
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.util.*
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(
    basePackages = ["fs.playground.jpa"],
    enableDefaultTransactions = false
)
@EnableTransactionManagement
class JpaConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    fun jpaDataSource(): DataSource {
        return DataSourceBuilder.create().type(HikariDataSource::class.java).build()
    }

    @Bean
    fun entityManagerFactory(@Qualifier("jpaDataSource") jpaDataSource: DataSource): LocalContainerEntityManagerFactoryBean {
        val em = LocalContainerEntityManagerFactoryBean()
        em.dataSource = jpaDataSource
        em.setPackagesToScan("fs.playground.jpa")
        val vendorAdapter: JpaVendorAdapter = HibernateJpaVendorAdapter()
        em.jpaVendorAdapter = vendorAdapter
        em.setJpaProperties(additionalProperties())
        return em
    }

    fun additionalProperties(): Properties {
        val properties = Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect")
        properties.setProperty("hibernate.generate_statistics", "true")

        return properties;
    }

    @Bean
    fun transactionManager(dataSource: DataSource): PlatformTransactionManager? {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = entityManagerFactory(dataSource).getObject()
        return transactionManager
    }
}