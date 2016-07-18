package fs;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@Configuration
public class MonitoringConfig {

    @Autowired
    private MetricRegistry registry;

    @Bean
    public JmxReporter jmxReporter() {
        JmxReporter reporter = JmxReporter.forRegistry(registry).build();
        reporter.start();
        return reporter;
    }

    @Bean
    GraphiteReporter graphite(@Value("${graphite.prefix}") String prefix,
                              @Value("${graphite.url}") URL url,
                              @Value("${graphite.port}") int port,
                              MetricRegistry registry) {
        GraphiteReporter reporter = GraphiteReporter.forRegistry(registry)
                .prefixedWith(prefix)
                .build(new Graphite(url.getHost(), port));
        reporter.start(1, TimeUnit.SECONDS);
        return reporter;
    }
}
