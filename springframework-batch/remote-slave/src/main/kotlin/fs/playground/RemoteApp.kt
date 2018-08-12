package fs.playground

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RemoteApp

fun main(args: Array<String>) {
    runApplication<RemoteApp>(*args)
}