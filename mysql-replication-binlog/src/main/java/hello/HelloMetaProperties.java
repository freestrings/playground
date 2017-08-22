package hello;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "hello.meta")
public class HelloMetaProperties {

    /**
     * 모니터링 테이블 이름(table, table2, ...)
     */
    private Map<String, List<String>> availableTables = new HashMap<>();

    public Map<String, List<String>> getAvailableTables() {
        return availableTables;
    }
}
