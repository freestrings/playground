package fs.batch;

import fs.batch.dto.JobExecutionResult;
import fs.batch.exception.JobExecutionFail;
import fs.batch.exception.JobNotFound;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobLaunchService {

    @Autowired
    private JobRegistry jobRegistry;

    @Autowired
    private JobLauncher jobLauncher;

    public JobExecutionResult launch(String jobName) throws Exception {
        try {
            Job job = jobRegistry.getJob(jobName);
            JobExecution jobExecution = jobLauncher.run(job, new JobParametersBuilder().toJobParameters());

            Long id = jobExecution.getId();
            Long jobId = jobExecution.getJobId();
            String exitCode = jobExecution.getExitStatus().getExitCode();
            JobExecutionResult jobExecutionResult = new JobExecutionResult(id, jobId, exitCode, jobName);

            if (!exitCode.equals(ExitStatus.COMPLETED.getExitCode())) {
                throw new JobExecutionFail(jobName, jobExecutionResult);
            }

            return jobExecutionResult;
        } catch (NoSuchJobException e) {
            throw new JobNotFound(jobName);
        }
    }

}
