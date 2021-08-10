package fs.playground

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.redis.core.RedisKeyValueAdapter.EnableKeyspaceEvents
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

//@EnableRedisRepositories(enableKeyspaceEvents = EnableKeyspaceEvents.ON_STARTUP)
@SpringBootApplication
class SpringdataRedisindexedApplication

fun main(args: Array<String>) {
    runApplication<SpringdataRedisindexedApplication>(*args)
}
