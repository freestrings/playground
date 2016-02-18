package fs.theother;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(basePackages = "fs.theother", entityManagerFactoryRef = "theotherEntityManagerFactory", transactionManagerRef = "theotherTransactionManager")
@EnableTransactionManagement
public class TheOtherDataSourceConfiguration {

    @Autowired
    private JpaProperties jpaProperties;

    @Bean
    @ConfigurationProperties(prefix = "fs.datasource.theother")
    public DataSource theotherDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public EntityManager anotherEntityManager(EntityManagerFactoryBuilder builder, @Qualifier("theotherDataSource") DataSource theotherDataSource) {
        return theotherEntityManagerFactory(builder, theotherDataSource).getObject().createEntityManager();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean theotherEntityManagerFactory(
            EntityManagerFactoryBuilder builder, @Qualifier("theotherDataSource") DataSource theotherDataSource) {
        return builder.dataSource(theotherDataSource)
                .packages("fs.theother")
                .properties(jpaProperties.getHibernateProperties(theotherDataSource))
                .persistenceUnit("theotherPersistenceUnit")
                .build();
    }

    @Bean
    public PlatformTransactionManager theotherTransactionManager(@Qualifier("theotherEntityManagerFactory") EntityManagerFactory theotherEntityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(theotherEntityManagerFactory);
        return transactionManager;
    }
}
