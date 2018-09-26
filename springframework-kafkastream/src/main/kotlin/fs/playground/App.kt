package fs.playground

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
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
 * 3. ./gradlew clean build
 * 4. java -jar build/libs/springframework-kafkastream-0.0.1-SNAPSHOT.jar input|output|readAllTopic <topic name>
 */
@SpringBootApplication
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)

    if (args[0] == "input") {
        input()
    } else if (args[0] == "output") {
        output()
    } else if (args[0] == "readAllTopic") {
        readAllTopic(args[1])
    }

}

private fun input() {
    val props = Properties()
    props[StreamsConfig.APPLICATION_ID_CONFIG] = "streams-wordcount"
    props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
    props[StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG] = 0
    props[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String()::class.java.name
    props[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String()::class.java.name

    val builder = StreamsBuilder()
    val source = builder.stream<String, String>("streams-wordcount-input")
    val counts = source
            .flatMapValues { value ->
                println("split: $value")
                value.toLowerCase().split(" ")
            }
            .groupBy { key, value ->
                println("group by: $value")
                value
            }
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

private fun output() {
    val prop = Properties()
    prop[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
    prop[ConsumerConfig.GROUP_ID_CONFIG] = "wordcount-output-group"
    prop[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = Serdes.String().deserializer()::class.java.name
    prop[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = Serdes.Long().deserializer()::class.java.name

    val consumer = KafkaConsumer<String, Long>(prop)
    consumer.subscribe(listOf("streams-wordcount-output"))

    while (true) {
        val records = consumer.poll(1000)
        if (records.count() == 0) {
            continue
        }
        records.forEach { record ->
            println("word: ${record.key()}, count: ${record.value()}, partition: ${record.partition()}, offset: ${record.offset()}")
        }
        consumer.commitAsync()
    }

    consumer.close()
}

/**
 * 1. docker-compose exec kafka kafka-topics --zookeeper zookeeper:32181 --list
 * __confluent.support.metrics
 * __consumer_offsets
 * streams-wordcount-KSTREAM-AGGREGATE-STATE-STORE-0000000003-changelog
 * streams-wordcount-KSTREAM-AGGREGATE-STATE-STORE-0000000003-repartition
 * streams-wordcount-input
 * streams-wordcount-output
 *
 * 컨슈머를 안전하게 닫으면 뭐가좋은지 해 봤음. => 모르겠음.
 */
private fun readAllTopic(topic: String) {
    val prop = Properties()
    prop[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
    prop[ConsumerConfig.GROUP_ID_CONFIG] = UUID.randomUUID().toString()
    prop[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
    prop[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = Serdes.String().deserializer()::class.java.name

    if (topic.endsWith("output")) {
        prop[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = Serdes.Long().deserializer()::class.java.name
    } else if (topic.endsWith("input")) {
        prop[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = Serdes.String().deserializer()::class.java.name
    } else {
        throw IllegalArgumentException("streams-wordcount-input|streams-wordcount-output 두 토픽만 됨")
    }

    val consumer = KafkaConsumer<String, Long>(prop)
    val latch = CountDownLatch(1)
    val thread = object : Thread("read-all") {
        override fun run() {
            println("start consumer")
            consumer.subscribe(listOf(topic))
            while (true) {
                if (latch.count == 0L) {
                    consumer.unsubscribe()
                    consumer.close()
                    Thread.sleep(1000)
                    break
                }
                val records = consumer.poll(100)
                if (records.count() != 0) {
                    records.forEach { record ->
                        println("key: ${record.key()}, value: ${record.value()}, partition: ${record.partition()}, offset: ${record.offset()}")
                    }
                }
            }
        }
    }

    Runtime.getRuntime().addShutdownHook(
            object : Thread("read-all-hook") {
                override fun run() {
                    println("shutdown")
                    latch.countDown()
                    Thread.sleep(1000)
                }
            })

    try {
        thread.start()
        latch.await()
    } catch (e: Throwable) {
        System.exit(1)
    }

    System.exit(0)
}