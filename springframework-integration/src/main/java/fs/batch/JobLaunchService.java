package fs.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobLaunchService {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job prahaJob;

    @Autowired
    private Job prahaMemberActionCntInInfosJob;

    public void praha() throws Exception {
        run(prahaJob);
    }

    public void prahaMemberActionCntInInfosJob() throws Exception {
        run(prahaMemberActionCntInInfosJob);
    }

    private void run(Job job) throws Exception {
        jobLauncher.run(job, new JobParametersBuilder().toJobParameters());
    }

}
