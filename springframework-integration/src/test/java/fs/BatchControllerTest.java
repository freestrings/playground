package fs;

import fs.batch.job.PrahaJobConfig;
import fs.batch.job.PrahaMemberActionCntInInfosJobConfig;
import fs.config.MockResourceConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.List;

/**
 * 로컬 웹서버는 별도 실행 해야 함 @see README.md
 */
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(
        classes = {MockResourceConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {
        "server.port=8081",
        "fs.outbound.port=8081"
})
@ActiveProfiles("test")
public class BatchControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void testJobOnController() throws Exception {
        List<String> jobNames = Arrays.asList(
                PrahaJobConfig.JOB_NAME,
                PrahaMemberActionCntInInfosJobConfig.JOB_NAME);

        for (String jobName : jobNames) {
            performTest(jobName);
        }
    }

    private String getBatchJobPrefix() {
        return "/batch/jobs/";
    }

    private String getTestJobUrl(String jobName) {
        return getBatchJobPrefix() + jobName;
    }

    private void performTest(String jobName) throws Exception {
        mvc.perform(MockMvcRequestBuilders.post(getTestJobUrl(jobName)))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("$.jobName").value(jobName))
                .andExpect(MockMvcResultMatchers.jsonPath("$.jobId").isNumber());
    }

}
