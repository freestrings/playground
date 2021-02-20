package fs.playground

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WebfluxCoroutineJdbcApplication

fun main(args: Array<String>) {
	runApplication<WebfluxCoroutineJdbcApplication>(*args)
}
