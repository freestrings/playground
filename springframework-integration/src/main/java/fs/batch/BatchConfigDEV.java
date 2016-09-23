package fs.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

@Profile({"local", "development"})
@Configuration
@EnableBatchProcessing
public class BatchConfigDEV {

    @Bean
    @Primary
    public DataSource hsqldbDataSource() throws SQLException {
        final SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriver(new org.hsqldb.jdbcDriver());
        dataSource.setUrl("jdbc:hsqldb:mem:mydb");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

}
