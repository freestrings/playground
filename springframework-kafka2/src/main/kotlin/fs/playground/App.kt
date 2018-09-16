package fs.playground

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.config.ContainerProperties
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.*

@SpringBootApplication
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}

data class Dummy(
        var value: String = ""
)

@Configuration
class KafkaConfig {

    @Value("\${kafka.bootstrap-servers}")
    private val bootstrapServers: String? = null

    @Value("\${kafka.topic.requestreply-topic}")
    private val requestReplyTopic: String? = null

    @Value("\${kafka.consumergroup}")
    private val consumerGroup: String? = null

    @Bean
    fun producerConfigs(): Map<String, Any?> {
        val props = HashMap<String, Any?>()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        return props
    }

    @Bean
    fun consumerConfigs(): Map<String, Any?> {
        val props = HashMap<String, Any?>()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ConsumerConfig.GROUP_ID_CONFIG] = "helloworld"
        return props
    }

    @Bean
    fun producerFactory(): ProducerFactory<String, Dummy> {
        return DefaultKafkaProducerFactory(producerConfigs())
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Dummy> {
        return KafkaTemplate(producerFactory())
    }

    @Bean
    fun replyKafkaTemplate(pf: ProducerFactory<String, Dummy>,
                           container: KafkaMessageListenerContainer<String, Dummy>
    ): ReplyingKafkaTemplate<String, Dummy, Dummy> {
        return ReplyingKafkaTemplate(pf, container)

    }

    @Bean
    fun replyContainer(cf: ConsumerFactory<String, Dummy>): KafkaMessageListenerContainer<String, Dummy> {
        val containerProperties = ContainerProperties(requestReplyTopic)
        return KafkaMessageListenerContainer(cf, containerProperties)
    }

    @Bean
    fun consumerFactory(): ConsumerFactory<String, Dummy> {
        return DefaultKafkaConsumerFactory(consumerConfigs(),
                                           StringDeserializer(),
                                           JsonDeserializer(Dummy::class.java))
    }

    @Bean
    fun kafkaListenerContainerFactory(): KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Dummy>> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, Dummy>()
        factory.setConsumerFactory(consumerFactory())
        factory.setReplyTemplate(kafkaTemplate())
        return factory
    }

}

@Component
class ReplyingKafkaConsumer {

    @KafkaListener(topics = ["\${kafka.topic.request-topic}"])
    @SendTo
    fun listen(request: Dummy): Dummy {
        return Dummy(request.value + "!")
    }

}

@RestController
class Controller {

    @Autowired
    lateinit var kafkaTemplate: ReplyingKafkaTemplate<String, Dummy, Dummy>

    @Value("\${kafka.topic.request-topic}")
    var requestTopic: String? = null

    @Value("\${kafka.topic.requestreply-topic}")
    var requestReplyTopic: String? = null

    @PostMapping(value = ["/hello"],
                 produces = [MediaType.APPLICATION_JSON_VALUE],
                 consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun hello(@RequestBody request: Dummy): Dummy {
        val record = ProducerRecord<String, Dummy>(requestTopic, request)
        record.headers().add(RecordHeader(KafkaHeaders.REPLY_TOPIC, requestReplyTopic!!.toByteArray()))
        val sendAndReceive = kafkaTemplate.sendAndReceive(record)
        val sendResult = sendAndReceive.sendFuture.get()
        sendResult.producerRecord.headers().forEach { header ->
            println("${header.key()} : ${header.value()}")
        }
        val consumerRecord = sendAndReceive.get()
        return consumerRecord.value()
    }
}