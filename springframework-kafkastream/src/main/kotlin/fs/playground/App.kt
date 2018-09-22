package fs.playground

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Produced
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.*
import java.util.concurrent.CountDownLatch

/**
 * 1. docker-compose up
 * 2. docker-compose exec kafka kafka-console-producer --broker-list localhost:9092 --topic streams-wordcount-input
 * 3. run App
 * 4. run PrintResult
 */
@SpringBootApplication
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)

    val props = Properties()
    props[StreamsConfig.APPLICATION_ID_CONFIG] = "streams-wordcount"
    props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
    props[StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG] = 0
    props[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String()::class.java.name
    props[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String()::class.java.name

    val builder = StreamsBuilder()
    val source = builder.stream<String, String>("streams-wordcount-input")
    val counts = source
            .flatMapValues { value -> value.toLowerCase().split(" ") }
            .groupBy { key, value -> value }
            .count()

    counts.toStream().to("streams-wordcount-output", Produced.with(Serdes.String(), Serdes.Long()))

    val streams = KafkaStreams(builder.build(), props)
    val latch = CountDownLatch(1)

    Runtime.getRuntime().addShutdownHook(
            object : Thread("streams-wordcount-shutdown-hook") {
                override fun run() {
                    streams.close()
                    latch.countDown()
                }
            })

    try {
        streams.start()
        latch.await()
    } catch (e: Throwable) {
        System.exit(1)
    }

    System.exit(0)
}
