package sessionwindow

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.Serdes
import java.time.Duration
import java.util.*

/**
 * 컨슈머그룹 ID가 달라지면 메시지 처음부터 죄다 읽어옴
 */
fun main() {
    val consumerProp = Properties()
    consumerProp[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
    consumerProp[ConsumerConfig.GROUP_ID_CONFIG] = "session-windows-consumer3"
    consumerProp[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
    consumerProp[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = Serdes.String().deserializer().javaClass
    consumerProp[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = Serdes.Long().deserializer().javaClass

    val consumer = KafkaConsumer<String, Long>(consumerProp)
    consumer.subscribe(arrayListOf("play-events-per-session"))
    var received = 0
    while (true) {
        val records = consumer.poll(Duration.ofMillis(Long.MAX_VALUE))
        println("<-----------------")
        records.forEach {
            println("${it.key()} = ${it.value()} [${records.count()}]")
        }
        println("----------------->")
        received += records.count()
    }

}
