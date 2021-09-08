package sumlambda

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.IntegerDeserializer
import java.time.Duration
import java.util.*


fun main() {
    val consumer = KafkaConsumer<Int, Int>(Properties().apply {
        put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer::class.java)
        put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer::class.java)
        put(ConsumerConfig.GROUP_ID_CONFIG, "sum-lambda-example-consumer")
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    })

    consumer.subscribe(Collections.singleton("sum-of-odd-numbers-topic"))

    while (true) {
        val records = consumer.poll(Duration.ofMillis(Long.MAX_VALUE))
        println("<-------------------")
        for (record in records) {
            println("Current sum of odd numbers is:" + record.value())
        }
        println("------------------->")
    }
}