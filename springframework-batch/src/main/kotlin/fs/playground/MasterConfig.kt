package fs.playground;

import org.apache.activemq.ActiveMQConnectionFactory
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.partition.support.SimplePartitioner
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration
import org.springframework.batch.integration.partition.RemotePartitioningMasterStepBuilderFactory
import org.springframework.batch.item.ExecutionContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.jms.dsl.Jms


@Profile("remote")
@Configuration
@EnableBatchProcessing
@EnableBatchIntegration
class MasterConfig(
        @Autowired private val masterStepBuilderFactory: RemotePartitioningMasterStepBuilderFactory,
        @Autowired private val jobBuilderFactory: JobBuilderFactory,
        @Autowired private val stepBuilderFactory: StepBuilderFactory,
        @Value("\${activemq.broker-url}") private val brokerUrl: String
) {

    val logger = LoggerFactory.getLogger(MasterConfig::class.java)

    @Bean
    fun connectionFactory(): ActiveMQConnectionFactory {
        val connectionFactory = ActiveMQConnectionFactory()
        connectionFactory.setBrokerURL(this.brokerUrl)
        connectionFactory.setTrustAllPackages(true)
        return connectionFactory
    }

    @Bean
    fun requests(): DirectChannel {
        return DirectChannel()
    }

    @Bean
    fun outboundFlow(): IntegrationFlow {
        return IntegrationFlows
                .from(requests())
                .handle(Jms.outboundAdapter(connectionFactory()).destination("requests"))
                .get()
    }

    @Bean
    fun replies(): DirectChannel {
        return DirectChannel()
    }

    @Bean
    fun inboundFlow(): IntegrationFlow {
        return IntegrationFlows
                .from(Jms.messageDrivenChannelAdapter(connectionFactory()).destination("replies"))
                .channel(replies())
                .get()
    }

    @Bean
    fun masterStep(): Step? {
        return this.masterStepBuilderFactory.get("masterStep")
                .partitioner("workerStep", BasicPartitioner())
                .gridSize(3)
                .outputChannel(requests())
                .inputChannel(replies())
                .build()
    }

    @Bean
    fun remotePartitioningJob(): Job? {
        return this.jobBuilderFactory.get("remotePartitioningJob")
                .start(masterStep())
                .build()
    }

}

class BasicPartitioner : SimplePartitioner() {
    private val PARTITION_KEY = "partition"

    override fun partition(gridSize: Int): MutableMap<String, ExecutionContext> {
        val partitions = super.partition(gridSize)
        var i = 0
        for ((_, context) in partitions) {
            context.put(PARTITION_KEY, PARTITION_KEY + (i++))
        }
        return partitions
    }
}
