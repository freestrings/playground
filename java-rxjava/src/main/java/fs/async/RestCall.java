package fs.async;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestCall {

    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity<String> call(String tag) {
        return restTemplate.getForEntity("http://localhost:8080/test_delay?tag=" + tag, String.class);
    }

}
