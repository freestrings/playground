package fs.playground.zkcurator

import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.recipes.shared.SharedCount
import org.apache.curator.retry.RetryNTimes
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component

@SpringBootApplication
class ZkCuratorApplication

fun main(args: Array<String>) {
    val ctx = runApplication<ZkCuratorApplication>(*args)
    val service = ctx.getBean(Service::class.java)
    service.testa()
}

@Component
class Service {

    fun testa() {
        val sleepMsBetweenRetries = 100
        val maxRetries = 3
        val retryPolicy = RetryNTimes(maxRetries, sleepMsBetweenRetries)
        val client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", retryPolicy)
        client.start()
        val counter = SharedCount(client, "/counters/A", 0)
        counter.start()
        counter.count = counter.count + 1
        println(counter.count)
    }
}
