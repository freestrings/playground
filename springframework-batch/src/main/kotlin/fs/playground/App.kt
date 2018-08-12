package fs.playground

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.*

@SpringBootApplication
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}

@Component
class Runner(
        @Autowired val context: ApplicationContext
) : CommandLineRunner {

    private fun jobParameters(key: String) = JobParametersBuilder()
            .addString(key, // BATCH_JOB_EXECUTION_PARAMS.key_name
                    SimpleDateFormat("yyyy-MM-dd").format(Date()))
            .toJobParameters()

    /**
     * [jobName]
     * failedJob1OnStep2 => job1이 step2에서 실패.
     * notRestartableJob1 => preventRestart 하면 재시작 안됨.
     * completedJob1 => job1을 실패한 step2 부터 시작하고 끝냄.
     *
     * [jobKeyName]
     * jobKeyName 같아야 테스트 가능함.
     */
    override fun run(vararg args: String?) {
        if (args.size != 2) {
            println("#########################################")
            println("")
            println("")
            println("Program arguments: [jobName] [jobKeyName]")
            println("")
            println("")
            println("#########################################")
        } else {
            val jobName = args[0]!!
            val jobKeyName = args[1]!!

            context.getBean(JobLauncher::class.java)
                    .run(context.getBean(jobName, Job::class.java), jobParameters(jobKeyName))
        }
    }

}
