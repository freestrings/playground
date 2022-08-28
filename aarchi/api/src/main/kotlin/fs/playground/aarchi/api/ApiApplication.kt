package fs.playground.aarchi.api

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class AarchiApplication

fun main(args: Array<String>) {
    runApplication<AarchiApplication>(*args)
}

@RestController
class ApiController {

    final val log = LoggerFactory.getLogger(ApiController::class.java)

    @GetMapping("/v1/hey")
    fun hey(): String {
        log.info("I say why?!")
        return "why?"
    }
}