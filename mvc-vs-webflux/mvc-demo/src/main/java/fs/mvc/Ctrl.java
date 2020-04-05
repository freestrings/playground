package fs.mvc;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController("/")
public class Ctrl {

    private RestTemplate restTemplate;

    public Ctrl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/http")
    public String http(@RequestParam("v") String v) {
        System.out.println(Thread.currentThread() + " < " + v);
        restTemplate.exchange(
                "http://delay:8080/?delay=300&time=" + System.currentTimeMillis(),
                HttpMethod.GET,
                new HttpEntity<String>(new HttpHeaders()),
                String.class
        );
        System.out.println(Thread.currentThread() + " > " + v);
        return v;
    }

    @GetMapping("/blocking")
    public String blocking(@RequestParam("v") String v) throws InterruptedException {
        System.out.println(Thread.currentThread() + " > " + v);
        Thread.sleep(300);
        return v;
    }
}
