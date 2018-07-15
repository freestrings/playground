package fs.playground

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import javax.sql.DataSource


@Configuration
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

}