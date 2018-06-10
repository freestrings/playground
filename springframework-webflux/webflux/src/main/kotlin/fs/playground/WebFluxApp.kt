package fs.playground

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient

@SpringBootApplication
class WebFluxApp {

    @Bean
    fun webClient() = WebClient.builder().baseUrl("http://localhost:8081").build()
}

@RestController
class RestController {

    @Autowired
    lateinit var webClient: WebClient

    @GetMapping("/")
    fun get() = webClient.get().uri("/test.txt").retrieve().bodyToMono(String::class.java)

}

fun main(args: Array<String>) {
    runApplication<WebFluxApp>(*args)
}