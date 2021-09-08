package sumlambda

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.IntegerSerializer
import java.util.*

fun main() {
    val producer = KafkaProducer<Int, Int>(Properties().apply {
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer::class.java)
        put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, IntegerSerializer::class.java)
    })

    (0..100).map {
        ProducerRecord("numbers-topic", it, it)
    }.forEach(producer::send)

    // 플러시 안하면 안보냄
    producer.flush()
}