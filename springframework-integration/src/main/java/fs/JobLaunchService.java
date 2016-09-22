package fs;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobLaunchService {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job messageAJob;

    @Autowired
    private Job messageBJob;

    public void runMessageA_Job() throws JobParametersInvalidException,
            JobExecutionAlreadyRunningException,
            JobRestartException,
            JobInstanceAlreadyCompleteException {
        jobLauncher.run(messageAJob, new JobParametersBuilder().toJobParameters());
    }

    public void runMessageB_Job() throws JobParametersInvalidException,
            JobExecutionAlreadyRunningException,
            JobRestartException,
            JobInstanceAlreadyCompleteException {
        jobLauncher.run(messageBJob, new JobParametersBuilder().toJobParameters());
    }
}
