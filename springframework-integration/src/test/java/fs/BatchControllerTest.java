package fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * 로컬 웹서버는 별도 실행 해야 함 @see README.md
 */
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("local")
public class BatchControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void testPraha() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/batch/praha"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
