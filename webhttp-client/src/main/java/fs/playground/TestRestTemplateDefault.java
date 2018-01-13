package fs.playground;

import org.springframework.web.client.RestTemplate;

public class TestRestTemplateDefault implements Testable {


    @Override
    public void doTest(int loop, String url) {
        final RestTemplate restTemplate = createRestTemplate();

        ThreadedTest.run(loop, () -> {
            try {
                restTemplate.getForObject(url + "?" + System.currentTimeMillis(), String.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public RestTemplate createRestTemplate() {
        return new RestTemplate();
    }
}
