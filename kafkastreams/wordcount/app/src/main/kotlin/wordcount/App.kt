package wordcount

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Produced
import java.util.*
import java.util.regex.Pattern

fun main() {
    val prop = Properties()
    prop[StreamsConfig.APPLICATION_ID_CONFIG] = "wordcount"
    prop[StreamsConfig.CLIENT_ID_CONFIG] = "wordcount-client"
    prop[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
    prop[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass.name
    prop[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String().javaClass.name
    // 10 초
    prop[StreamsConfig.COMMIT_INTERVAL_MS_CONFIG] = 10 * 1000
    // disable
    prop[StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG] = 0
    // 상태를 저장할 임시 디렉토리
    prop[StreamsConfig.STATE_DIR_CONFIG] = "/home/han/Documents/playground/kafkastreams/wordcount/tmp"

    val pattern = Pattern.compile("\\W+")
    val builder = StreamsBuilder()
    val textLines = builder.stream<String, String>("streams-plaintext-input")

    val wordCount = textLines.flatMapValues { it ->
        pattern.split(it.toLowerCase()).toList()
    }
        .groupBy { key, value -> value }
        .count()

    wordCount.toStream().to(
        "streams-wordcount-output",
        Produced.with(Serdes.String(), Serdes.Long())
    )

    val streams = KafkaStreams(builder.build(), prop)
    streams.cleanUp()
    streams.start()

    Runtime.getRuntime().addShutdownHook(Thread(streams::close))
}
