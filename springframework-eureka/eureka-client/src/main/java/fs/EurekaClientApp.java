package fs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EnableDiscoveryClient
@SpringBootApplication
public class EurekaClientApp {

    public static void main(String[] args) {
        SpringApplication.run(EurekaClientApp.class, args);
    }

}

@RestController
class ServiceInstanceRestController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @RequestMapping("/service-instances/{applicationName}")
    public List<ServiceInstance> serviceInstancesByApplicationName(
            @PathVariable String applicationName) {

        List<ServiceInstance> instances = this.discoveryClient.getInstances(applicationName);

        instances.forEach(
                si -> System.out.println(si.getHost() + ":" + si.getPort())
        );

        return instances;
    }

    @RequestMapping("/myenv")
    public Map<String, String> hostaddress() throws UnknownHostException {
        HashMap map = new HashMap();
        map.put("hostaddress", InetAddress.getLocalHost().getHostAddress());
        map.put("hostname", InetAddress.getLocalHost().getHostName());
        return map;
    }
}