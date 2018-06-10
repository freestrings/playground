package fs.playground

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@SpringBootApplication
class RestTeamplateApp {

    @Bean
    fun restTemplate() = RestTemplateBuilder().build()
}

@RestController
class RestController {

    @Autowired
    lateinit var restTemplate: RestTemplate

    @GetMapping("/")
    fun get() = restTemplate.getForObject("http://localhost:8081/test.txt", String::class.java)
}

fun main(args: Array<String>) {
    SpringApplication.run(RestTeamplateApp::class.java, *args)
}