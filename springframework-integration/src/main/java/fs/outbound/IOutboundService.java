package fs.outbound;

import fs.outbound.dto.MemberActionCntInfosDTO;
import fs.outbound.dto.PrahaDTO;
import fs.outbound.dto.ResponseDTO;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.GatewayHeader;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway(defaultRequestChannel = "inHttp")
public interface IOutboundService {

    @Gateway(headers = @GatewayHeader(name = "routePath", value = "praha"))
    ResponseDTO<PrahaDTO> praha(String request);

    @Gateway(headers = @GatewayHeader(name = "routePath", value = "praha/memberActionCntInfos"))
    ResponseDTO<MemberActionCntInfosDTO> praha_memberActionCntInfos(String request);

}
