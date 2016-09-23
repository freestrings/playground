package fs.schedule;

import fs.batch.JobLaunchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@Profile("production")
@EnableScheduling
public class BatchScheduleConfig {

    @Autowired
    private JobLaunchService jobLaunchService;

    @Scheduled(cron = "${fs.batch.schedule.praha}")
    public void prahaJob() throws Exception {
        log.info("Scheduled prahaJob");
        jobLaunchService.praha();
    }

    @Scheduled(cron = "${fs.batch.schedule.prahaMemberActionCntInInfos}")
    public void prahaMemberActionCntInInfosJob() throws Exception {
        log.info("Scheduled prahaJob");
        jobLaunchService.praha();
    }
}
