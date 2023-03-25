package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class TestaApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestaApplication.class, args);
	}


}
