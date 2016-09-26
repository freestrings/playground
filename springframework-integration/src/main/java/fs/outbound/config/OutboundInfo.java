package fs.outbound.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "fs.outbound")
public class OutboundInfo {

    private String protocol;
    private String host;
    private int port;
    private String path;
    private String user;
    private String password;

}
