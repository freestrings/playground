package fs.playground;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class TestHttClientDefault implements Testable {

    @Override
    public void doTest(int loop, String url) {
        final CloseableHttpClient httpClient = createHttpClient();

        ThreadedTest.run(loop, () -> {
            try {
                CloseableHttpResponse response = httpClient.execute(new HttpGet(url + "?" + System.currentTimeMillis()));
                HttpEntity entity = response.getEntity();
                EntityUtils.consume(entity);
                response.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    public CloseableHttpClient createHttpClient() {
        return HttpClients.createDefault();
    }
}
