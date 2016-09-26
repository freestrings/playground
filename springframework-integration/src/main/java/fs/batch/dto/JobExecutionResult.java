package fs.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class JobExecutionResult {

    private long id;

    private long jobId;

    private String exitCode;

    private String jobName;
}
