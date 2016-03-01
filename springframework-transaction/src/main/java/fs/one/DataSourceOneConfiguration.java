package fs.one;

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
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(basePackages = "fs.one.repository", entityManagerFactoryRef = "entityManagerOne")
public class DataSourceOneConfiguration {

    @Autowired
    private JpaProperties jpaProperties;

    @Primary
    @ConfigurationProperties("fs.atomikos.datasource.one")
    @Bean
    public DataSource dataSourceOne() {
        return new AtomikosDataSourceBean();
    }

    @Bean
    @DependsOn("transactionManager")
    public LocalContainerEntityManagerFactoryBean entityManagerOne(
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

        DataSource dataSource = dataSourceOne();
        entityManager.setJtaDataSource(dataSource);
        entityManager.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        entityManager.setPackagesToScan("fs.one.model");
        entityManager.setPersistenceUnitName("persistenceUnitOne");
        entityManager.setJpaPropertyMap(jpaProperties.getHibernateProperties(dataSource));
        return entityManager;
    }
}
