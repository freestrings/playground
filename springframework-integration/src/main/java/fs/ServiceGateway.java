package fs;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.GatewayHeader;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway(name = "serviceGateway", defaultRequestChannel = "inHttp")
public interface ServiceGateway {

    @Gateway(headers = @GatewayHeader(name = "routePath", value = "a"))
    MessageA getA(String request);

    @Gateway(headers = @GatewayHeader(name = "routePath", value = "b"))
    MessageB getB(String request);

}
