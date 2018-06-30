package fs.playground.optimisticlock

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class OptimisticLockApplication

fun main(args: Array<String>) {
    runApplication<OptimisticLockApplication>(*args)
}