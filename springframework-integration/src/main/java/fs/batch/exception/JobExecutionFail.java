package fs.batch.exception;

import fs.batch.dto.JobExecutionResult;

public class JobExecutionFail extends RuntimeException {

    public JobExecutionFail(String jobName, JobExecutionResult jobExecutionResult) {
        super(String.format("Job[%s]: id=%s, jobId=%s, exitcode=%s",
                jobName,
                jobExecutionResult.getId(),
                jobExecutionResult.getExitCode(),
                jobExecutionResult.getExitCode()));
    }
}
