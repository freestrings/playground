package fs.playground

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.Serdes
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.*

@SpringBootApplication
class PrintResult

fun main(args: Array<String>) {
    runApplication<App>(*args)

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