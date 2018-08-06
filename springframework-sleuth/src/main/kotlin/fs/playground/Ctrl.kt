package fs.playground

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class Ctrl {

    private val log = LoggerFactory.getLogger(Ctrl::class.java)

    @GetMapping("/hello")
    fun hello(): String {
        val msg = "Hello - ${System.currentTimeMillis()}"
        log.info(msg)
        return msg
    }
}