package fs.playground;

import com.rabbitmq.jms.admin.RMQConnectionFactory
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration
import org.springframework.batch.integration.partition.RemotePartitioningWorkerStepBuilderFactory
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.jms.dsl.Jms
import javax.jms.ConnectionFactory


@Configuration
@EnableBatchProcessing
@EnableBatchIntegration
class RemoteSlaveConf(
        @Autowired private val workerStepBuilderFactory: RemotePartitioningWorkerStepBuilderFactory
) {

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
    fun inboundFlow(connectionFactory: ConnectionFactory): IntegrationFlow {
        return IntegrationFlows
                .from(Jms.messageDrivenChannelAdapter(connectionFactory).destination("requests"))
                .channel(requests())
                .get()
    }

    @Bean
    fun replies(): DirectChannel {
        return DirectChannel()
    }

    @Bean
    fun outboundFlow(connectionFactory: ConnectionFactory): IntegrationFlow {
        return IntegrationFlows
                .from(replies())
                .handle(Jms.outboundAdapter(connectionFactory).destination("replies"))
                .get()
    }

    @Bean
    fun workerStep(): Step {
        return this.workerStepBuilderFactory.get("workerStep")
                .inputChannel(requests())
                .outputChannel(replies())
                .tasklet(tasklet(null))
                .build()
    }

    @Bean
    @StepScope
    fun tasklet(@Value("#{stepExecutionContext['partition']}") partition: String?): Tasklet {
        return object : Tasklet {
            override fun execute(contribution: StepContribution?, chunkContext: ChunkContext?): RepeatStatus {
                println("processing $partition")
                return RepeatStatus.FINISHED
            }
        }
    }
}
