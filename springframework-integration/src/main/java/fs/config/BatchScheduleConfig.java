package fs.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class BatchScheduleConfig {

    @Autowired
    private JobLaunchService jobLaunchService;

    @Scheduled(fixedRate = 3000)
    public void scheduleForMessageA_Job() throws Exception {
        System.out.println("######################################Run Scheduled Job: messageAJob");
        jobLaunchService.runMessageA_Job();
    }

}
