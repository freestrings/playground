package fs.async;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
public class DummyDelay {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @RequestMapping("/test_delay")
    @ResponseBody
    public String delay(@RequestParam("tag") String tag) {
        long time = System.currentTimeMillis();
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {
        }
        return String.format("OK[%s] %s", tag, System.currentTimeMillis() - time);
    }
}
