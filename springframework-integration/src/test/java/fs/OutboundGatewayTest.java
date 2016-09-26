package fs;

import fs.config.MockResourceConfig;
import fs.outbound.IOutboundService;
import fs.outbound.dto.MemberActionCntInfosDTO;
import fs.outbound.dto.PrahaDTO;
import fs.outbound.dto.ResponseDTO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 로컬 웹서버는 별도 실행 해야 함 @see README.md
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {MockResourceConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {
        "server.port=8082",
        "fs.outbound.port=8082"
})
@ActiveProfiles("test")
public class OutboundGatewayTest {

    @Autowired
    private IOutboundService outboundService;

    @Test
    public void testPraha() throws Exception {
        ResponseDTO<PrahaDTO> response = outboundService.praha("");
        String id = response.getId();
        Assert.assertTrue(id.equals("praha"));
    }

    @Test
    public void testPrahaMemberActionCntInfos() throws Exception {
        ResponseDTO<MemberActionCntInfosDTO> response = outboundService.prahaMemberActionCntInfos("");
        String id = response.getId();
        Assert.assertTrue(id.equals("memberActionCntInfos"));
    }

}
