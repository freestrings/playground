package hello;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableCouchbaseRepositories
public class Config extends AbstractCouchbaseConfiguration {

    protected List<String> getBootstrapHosts() {
        return Arrays.asList("127.0.0.1");
    }

    protected String getBucketName() {
        return "customer";
    }

    protected String getBucketPassword() {
        return null;
    }
}
