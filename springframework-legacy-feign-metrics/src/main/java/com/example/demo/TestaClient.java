package com.example.demo;

import feign.Client;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "testa", url = "http://localhost:8888", configuration = TestaClientConfig.class)
public interface TestaClient {

    @GetMapping("/a.json")
    String a();
}
