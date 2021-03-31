package fs.playground

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ShardingsphereApplication

fun main(args: Array<String>) {
	runApplication<ShardingsphereApplication>(*args)
}
