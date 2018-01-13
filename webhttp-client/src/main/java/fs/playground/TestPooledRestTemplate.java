package fs.playground;

import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class TestPooledRestTemplate extends TestRestTemplateDefault {

    @Override
    public RestTemplate createRestTemplate() {
        CloseableHttpClient httpClient = new TestPooledHttClient().createHttpClient();
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    }
}
