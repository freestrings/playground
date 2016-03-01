package fs.two;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import fs.AtomikosJtaPlatform;
import org.hibernate.engine.transaction.spi.TransactionEnvironment;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.jpa.internal.EntityManagerFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(basePackages = "fs.two.repository", entityManagerFactoryRef = "entityManagerTwo")
public class DataSourceTwoConfiguration {

    @Autowired
    private JpaProperties jpaProperties;

    @ConfigurationProperties("fs.atomikos.datasource.two")
    @Bean
    public DataSource dataSourceTwo() {
        return new AtomikosDataSourceBean();
    }

    @Bean
    @DependsOn("transactionManager")
    public LocalContainerEntityManagerFactoryBean entityManagerTwo(
            @Qualifier("atomikosUserTransaction") final UserTransactionImp userTransactionImp,
            @Qualifier("atomikosTransactionManager") final UserTransactionManager userTransactionManager
    ) throws Throwable {

        LocalContainerEntityManagerFactoryBean entityManager = new LocalContainerEntityManagerFactoryBean() {

            protected void postProcessEntityManagerFactory(EntityManagerFactory emf, PersistenceUnitInfo pui) {
                EntityManagerFactoryImpl entityManagerFactory = (EntityManagerFactoryImpl) emf;
                SessionFactoryImpl sessionFactory = entityManagerFactory.getSessionFactory();
                TransactionEnvironment transactionEnvironment = sessionFactory.getTransactionEnvironment();
                AtomikosJtaPlatform atomikosJtaPlatform = (AtomikosJtaPlatform) transactionEnvironment.getJtaPlatform();
                atomikosJtaPlatform.setUserTransactionImp(userTransactionImp);
                atomikosJtaPlatform.setUserTransactionManager(userTransactionManager);
            }
        };

        DataSource dataSource = dataSourceTwo();
        entityManager.setJtaDataSource(dataSource);
        entityManager.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        entityManager.setPackagesToScan("fs.two.model");
        entityManager.setPersistenceUnitName("persistenceUnitTwo");
        entityManager.setJpaPropertyMap(jpaProperties.getHibernateProperties(dataSource));
        return entityManager;
    }
}
