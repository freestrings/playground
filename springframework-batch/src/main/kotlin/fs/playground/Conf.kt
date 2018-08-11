package fs.playground;

import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.step.builder.SimpleStepBuilder
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Function

@Configuration
@EnableBatchProcessing
class Conf(
        @Autowired private val jobBuilderFactory: JobBuilderFactory,
        @Autowired private val stepBuilderFactory: StepBuilderFactory
) {

    val logger = LoggerFactory.getLogger(Conf::class.java)

    @Bean
    fun failedJob1OnStep2(): Job? {
        val job = jobBuilderFactory.get("job1")
                .start(step1())
                .next(failedStep("step2"))
                .build()
        logger.info("#[Bean Init] - failedJob1OnStep2, isRestartable: ${job}, ${job.isRestartable}")
        return job
    }

    @Bean
    fun notRestartableJob1(): Job? {
        val job = jobBuilderFactory.get("job1")
                .preventRestart()
                .start(step1())
                .next(step2())
                .build()
        logger.info("#[Bean Init] - notRestartableJob1, isRestartable: ${job}, ${job.isRestartable}")
        return job
    }

    @Bean
    fun completedJob1(): Job? {
        val job = jobBuilderFactory.get("job1")
                .start(step1())
                .next(step2())
                .build()
        logger.info("#[Bean Init] - completedJob1, isRestartable: ${job}, ${job.isRestartable}")
        return job
    }

    fun reader(name: String, _count: Int): ItemReader<String> {
        var count = _count
        return ItemReader<String> {
            if (count-- > 0) {
                name
            } else {
                null
            }
        }
    }

    fun processor() = Function<String, String> { it }

    fun writer() = ItemWriter<String> { it -> println(it) }

    private fun stepBuilder(name: String, chunkSize: Int): SimpleStepBuilder<String, String>? {
        return stepBuilderFactory.get(name)
                .chunk<String, String>(chunkSize)
                .reader(reader(name, chunkSize + 1))
                .processor(processor())
                .writer(writer())
    }

    fun step(name: String, chunkSize: Int) = stepBuilder(name, chunkSize)!!.build()

    fun failedStep(name: String) = stepBuilder(name, 10)!!.reader({ throw Exception() }).build()

    @Bean
    fun step1() = step("step1", 10)

    @Bean
    fun step2() = step("step2", 10)
}
