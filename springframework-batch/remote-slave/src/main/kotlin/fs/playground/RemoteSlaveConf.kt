package fs.playground;

import com.rabbitmq.jms.admin.RMQConnectionFactory
import org.slf4j.LoggerFactory
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.step.item.SimpleChunkProcessor
import org.springframework.batch.integration.chunk.ChunkProcessorChunkHandler
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.jms.dsl.Jms
import javax.jms.ConnectionFactory

@Configuration
@EnableBatchProcessing
class RemoteSlaveConf {

    val logger = LoggerFactory.getLogger(RemoteSlaveConf::class.java)

    @Bean
    fun connectionFactory(): ConnectionFactory {
        return RMQConnectionFactory()
    }

    @Bean
    fun requests(): DirectChannel {
        return DirectChannel()
    }

    @Bean
    fun replies(): DirectChannel {
        return DirectChannel()
    }

    @Bean
    fun inboundFlow(): IntegrationFlow {
        return IntegrationFlows
                .from(Jms.messageDrivenChannelAdapter(connectionFactory()).destination("requests"))
                .channel(requests())
                .get()
    }

    @Bean
    fun outgoingReplies(): IntegrationFlow {
        return IntegrationFlows
                .from(replies())
                .handle(Jms.outboundAdapter(connectionFactory()).destination("replies"))
                .get();
    }

    @Bean
    fun s(): IntegrationFlow {
        val chunkProcessor = SimpleChunkProcessor<String, String>(
                ItemProcessor<String, String> { it },
                ItemWriter { it -> println(it) })

        val chunkProcessorChunkHandler = ChunkProcessorChunkHandler<String>()
        chunkProcessorChunkHandler.setChunkProcessor(chunkProcessor)

        return IntegrationFlows
                .from(requests())
                .handle(chunkProcessorChunkHandler, "handleChunk")
                .channel(replies())
                .get()
    }
}