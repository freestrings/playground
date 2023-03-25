package com.example.demo;

import feign.Client;
import feign.Feign;
import feign.httpclient.ApacheHttpClient;
//import feign.micrometer.MicrometerCapability;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.cloud.netflix.feign.FeignClientsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Import(FeignClientsConfiguration.class)
public class TestaClientConfig {

    @Bean
    public Feign testaFeign(Client client, MeterRegistry meterRegistry) {
        return Feign.builder()
                .client(client)
//                .addCapability(new MicrometerCapability(meterRegistry))
                .build();
    }

    @Primary
    @Bean
    public Client testaClient() {
        return new ApacheHttpClient();
    }
}
