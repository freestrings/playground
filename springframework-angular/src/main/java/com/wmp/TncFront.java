package com.wmp;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@EnableZuulProxy
@SpringBootApplication
@Controller
public class TncFront {

    @RequestMapping("/")
    public String home(Map<String, Object> model) {
        return "index.html";
    }

    public static void main(String... args) {
        SpringApplication app = new SpringApplication(TncFront.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }
}
