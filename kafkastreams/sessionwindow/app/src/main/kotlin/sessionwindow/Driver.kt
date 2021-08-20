package sessionwindow

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Serdes
import java.time.Duration
import java.util.*

fun main() {
    val playEventSerializer = SpecificAvroSerializer<PlayEvent>()
    val serdeConfig = mapOf(
        AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to "http://localhost:8081"
    )
    playEventSerializer.configure(serdeConfig, false)

    val producerProp = Properties()
    producerProp[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
    val playEventProducer = KafkaProducer(producerProp, Serdes.String().serializer(), playEventSerializer)
    val start = System.currentTimeMillis()

    playEventProducer.send(
        ProducerRecord(
            "play-events",
            null,
            start,
            "jo",
            PlayEvent(1, 10)
        )
    )

    playEventProducer.send(
        ProducerRecord(
            "play-events",
            null,
            start + App.INACTIVITY_GAP.toMillis(),
            "jo",
            PlayEvent(1, 10)
        )
    )

    playEventProducer.send(
        ProducerRecord(
            "play-events",
            null,
            start + App.INACTIVITY_GAP.toMillis() + 1,
            "jo",
            PlayEvent(1, 10)
        )
    )

    playEventProducer.send(
        ProducerRecord(
            "play-events",
            null,
            start + 2 * App.INACTIVITY_GAP.toMillis() + 1,
            "jo",
            PlayEvent(1, 10)
        )
    )

    playEventProducer.close()

    val consumerProp = Properties()
    consumerProp[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
    consumerProp[ConsumerConfig.GROUP_ID_CONFIG] = "session-windows-consumer"
    consumerProp[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
    consumerProp[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = Serdes.String().deserializer().javaClass
    consumerProp[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = Serdes.Long().deserializer().javaClass

    val consumer = KafkaConsumer<String, Long>(consumerProp)
    consumer.subscribe(arrayListOf("play-events-per-session"))
    var received = 0
    while (received < 4) {
        val records = consumer.poll(Duration.ofMillis(Long.MAX_VALUE))
        records.forEach {
            println("${it.key()} = ${it.value()} [${records.count()}]")
        }
        received += records.count()
    }
}
