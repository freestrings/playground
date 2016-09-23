package fs;

import fs.outbound.IOutboundService;
import fs.outbound.dto.PrahaDTO;
import fs.outbound.dto.ResponseDTO;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 로컬 웹서버는 별도 실행 해야 함 @see README.md
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("local")
public class OutboundGatewayTest {

    @Autowired
    private IOutboundService outboundService;

    @Test
    public void test() throws Exception {
        ResponseDTO<PrahaDTO> praha = outboundService.praha("");
        Assertions.assertThat(praha.getId().equals("praha"));
    }
}
