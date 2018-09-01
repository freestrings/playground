package fs.playground

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.zuul.EnableZuulProxy

@EnableZuulProxy
@SpringBootApplication
class WmpSettleApplication

fun main(args: Array<String>) {
    runApplication<WmpSettleApplication>(*args)
}
