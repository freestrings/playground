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
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.config.AggregatorFactoryBean
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.jms.JmsOutboundGateway
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
    fun jmsIn(): IntegrationFlow {
        return IntegrationFlows
                .from(Jms.messageDrivenChannelAdapter(connectionFactory())
                        .configureListenerContainer { c -> c.subscriptionDurable(false) }
                        .destination("requests"))
                .channel(requests())
                .get()
    }

    @Bean
    fun outgoingReplies(): IntegrationFlow {
        return IntegrationFlows.from("replies")
                .handle<JmsOutboundGateway>(Jms.outboundGateway(connectionFactory())
                        .requestDestination("replies"))
                .get()
    }

    @Bean
    @ServiceActivator(inputChannel = "requests")
    fun serviceActivator(): AggregatorFactoryBean {
        val aggregatorFactoryBean = AggregatorFactoryBean()
        aggregatorFactoryBean.setProcessorBean(chunkProcessorChunkHandler());
        aggregatorFactoryBean.setOutputChannel(replies());
        return aggregatorFactoryBean;
    }

    @Bean
    fun chunkProcessorChunkHandler(): ChunkProcessorChunkHandler<String> {
        val chunkProcessorChunkHandler = ChunkProcessorChunkHandler<String>();
        chunkProcessorChunkHandler.setChunkProcessor(SimpleChunkProcessor(
                ItemProcessor<String, String> { it },
                ItemWriter { it -> println(it) }))
        return chunkProcessorChunkHandler;
    }
}