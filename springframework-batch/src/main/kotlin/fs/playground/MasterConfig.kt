package fs.playground;

import com.rabbitmq.jms.admin.RMQConnectionFactory
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.step.builder.SimpleStepBuilder
import org.springframework.batch.integration.chunk.ChunkMessageChannelItemWriter
import org.springframework.batch.integration.chunk.RemoteChunkHandlerFactoryBean
import org.springframework.batch.item.ItemReader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.channel.QueueChannel
import org.springframework.integration.core.MessagingTemplate
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.jms.JmsOutboundGateway
import org.springframework.integration.jms.dsl.Jms
import javax.jms.ConnectionFactory

@Profile("remote")
@Configuration
@EnableBatchProcessing
class MasterConfig(
        @Autowired private val jobBuilderFactory: JobBuilderFactory,
        @Autowired private val stepBuilderFactory: StepBuilderFactory
) {

    val logger = LoggerFactory.getLogger(MasterConfig::class.java)

    @Bean
    fun connectionFactory(): ConnectionFactory {
        return RMQConnectionFactory()
    }

    @Bean
    fun requests(): DirectChannel {
        return DirectChannel()
    }

    @Bean
    fun replies(): QueueChannel {
        return QueueChannel()
    }

    @Bean
    fun jmsOutboundFlow(): IntegrationFlow {
        return IntegrationFlows.from("requests")
                .handle<JmsOutboundGateway>(Jms.outboundGateway(connectionFactory())
                        .requestDestination("requests"))
                .get()
    }

    @Bean
    fun messagingTemplate(): MessagingTemplate {
        val template = MessagingTemplate()
        template.setDefaultChannel(requests());
        template.setReceiveTimeout(2000);
        return template
    }

    @Bean
    fun jmsReplies(): IntegrationFlow {
        return IntegrationFlows
                .from(Jms.messageDrivenChannelAdapter(connectionFactory())
                        .configureListenerContainer { c -> c.subscriptionDurable(false) }
                        .destination("replies"))
                .channel(replies())
                .get()
    }

    @Bean
    fun chunkJob(): Job? {
        return this.jobBuilderFactory.get("chunkJob")
                .start(step1())
                .build()
    }

    @Bean
    @StepScope
    fun itemWriter(): ChunkMessageChannelItemWriter<String> {
        val chunkMessageChannelItemWriter = ChunkMessageChannelItemWriter<String>();
        chunkMessageChannelItemWriter.setMessagingOperations(messagingTemplate());
        chunkMessageChannelItemWriter.setReplyChannel(replies());
        return chunkMessageChannelItemWriter
    }

    @Bean
    fun chunkHandler(): RemoteChunkHandlerFactoryBean<String> {
        val remoteChunkHandlerFactoryBean = RemoteChunkHandlerFactoryBean<String>()
        remoteChunkHandlerFactoryBean.setChunkWriter(itemWriter())
        remoteChunkHandlerFactoryBean.setStep(step1());
        return remoteChunkHandlerFactoryBean
    }

    @Bean
    fun step1() = step("step1", 10)

    private fun reader(name: String, _count: Int): ItemReader<String> {
        var count = _count
        return ItemReader<String> {
            if (count-- > 0) {
                name
            } else {
                null
            }
        }
    }

    private fun stepBuilder(name: String, chunkSize: Int): SimpleStepBuilder<String, String>? {
        return stepBuilderFactory.get(name)
                .chunk<String, String>(chunkSize)
                .reader(reader(name, chunkSize + 1))
                .writer(itemWriter())
    }

    private fun step(name: String, chunkSize: Int) = stepBuilder(name, chunkSize)!!.build()

}