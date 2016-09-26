package fs.batch;

import fs.batch.dto.JobExecutionResult;
import fs.batch.exception.JobExecutionFail;
import fs.batch.exception.JobNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.bind.annotation.*;

/**
 * Rest로 배치를 실행
 */
@RestController
@RequestMapping("/batch")
public class BatchController {

    @Autowired
    private JobLaunchService jobLaunchService;

    @Autowired
    private MessageChannel inHttp;

    @PostMapping("/jobs/{jobName}")
    public JobExecutionResult jobs(@PathVariable String jobName) throws Exception {
        return jobLaunchService.launch(jobName);
    }

    @ExceptionHandler(value = JobNotFound.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "No such Job")
    public void noSuchJobException() {
    }

    @ExceptionHandler(value = JobExecutionFail.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Job Excecution Fail")
    public void jobExcecutionFailException() {
    }

}
