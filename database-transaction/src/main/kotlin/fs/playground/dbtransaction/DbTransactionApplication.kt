package fs.playground.dbtransaction

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DbTransactionApplication

fun main(args: Array<String>) {
    runApplication<DbTransactionApplication>(*args)
}
