package sessionwindow

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Serdes
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
    println("sent")

    playEventProducer.send(
        ProducerRecord(
            "play-events",
            null,
            start,
            "jo",
            PlayEvent(1, 10)
        )
    )
    println("sent")
    println("sleep 5 begin")
    Thread.sleep(5000)
    println("sleep done")

    playEventProducer.send(
        ProducerRecord(
            "play-events",
            null,
            start + 5000,
            "jo",
            PlayEvent(1, 10)
        )
    )
    println("sent")

    println("sleep 5 begin")
    Thread.sleep(5000)
    println("sleep done")

    playEventProducer.send(
        ProducerRecord(
            "play-events",
            null,
            start + 2 * 5000,
            "jo",
            PlayEvent(1, 10)
        )
    )
    println("sent")

    playEventProducer.close()
    println("close")
}
