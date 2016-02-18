package fs.one;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(basePackages = "fs.one")
@EnableTransactionManagement
public class OneDataSourceConfiguration {

    public static final String MASTER = "master";
    public static final String SLAVE = "slave";

    public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {
        @Override
        protected Object determineCurrentLookupKey() {
            return TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? SLAVE : MASTER;
        }
    }

    @Autowired
    private JpaProperties jpaProperties;

    @Bean
    @ConfigurationProperties(prefix = "fs.datasource.master")
    public DataSource oneMasterDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "fs.datasource.slave")
    public DataSource oneSlaveDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public DataSource oneRoutingDataSource(
            @Qualifier("oneMasterDataSource") DataSource dealMasterDataSource,
            @Qualifier("oneSlaveDataSource") DataSource dealSlaveDataSource) {
        ReplicationRoutingDataSource routingDataSource = new ReplicationRoutingDataSource();
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(MASTER, dealMasterDataSource);
        dataSourceMap.put(SLAVE, dealSlaveDataSource);
        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(dealMasterDataSource);
        return routingDataSource;
    }

    @Bean
    @Primary
    public DataSource oneDataSource(@Qualifier("oneRoutingDataSource") DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

    @Bean
    @Primary
    public EntityManager entityManager(EntityManagerFactoryBuilder builder, @Qualifier("oneDataSource") DataSource oneDataSource) {
        return entityManagerFactory(builder, oneDataSource).getObject().createEntityManager();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder, @Qualifier("oneDataSource") DataSource oneDataSource) {
        return builder.dataSource(oneDataSource)
                .packages("fs.one")
                .properties(jpaProperties.getHibernateProperties(oneDataSource))
                .persistenceUnit("onePersistenceUnit")
                .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }
}
