package hello;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import java.net.InetAddress;

@Configuration
public class Config {
    private String host = "127.0.0.1";

    private int port = 9300;

    private String clusterName = "customers";

    @Bean
    public Client client() throws Exception {

        Settings.Builder builder = Settings.builder();
        builder.put("cluster.name", clusterName);
        Settings settings = builder.build();

        return new PreBuiltTransportClient(settings)
                .addTransportAddress(
                        new InetSocketTransportAddress(InetAddress.getByName(host), port));
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() throws Exception {
        return new ElasticsearchTemplate(client());
    }

}
