package sessionwindow

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.Serdes
import java.time.Duration
import java.util.*

/**
 * 그룹ID를 같이 주면 컨슈머 그룹 => 컨슈머를 여러개 띄워도 메시지는 중복 전달 되지 않음
 *
 * records 숫자는 서버에서 받아온 메시지 숫자로 App에서 windowing한 값은 value로 넘어옴
 * => 5초안에 send 2건 하면, 컨슈머에 2건모두 전달되고 value가 1,2로 바뀜
 * <-----------------
 * jo@0 = 1 [2]
 * jo@0 = 2 [2]
 * ----------------->
 *
 * 프로듀서실행 후 컨슈머 붙이면 4건 찍힘
 * <-----------------
 * jo@0 = 1 [4]
 * jo@0 = 2 [4]
 * jo@0 = 1 [4]
 * jo@0 = 1 [4]
 * ----------------->
 */
fun main() {
    val consumerProp = Properties()
    consumerProp[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
    consumerProp[ConsumerConfig.GROUP_ID_CONFIG] = "session-windows-consumer"
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
