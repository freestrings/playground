package fs.playground;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class TestPooledHttClient extends TestHttClientDefault {

    @Override
    public CloseableHttpClient createHttpClient() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(300);
        cm.setDefaultMaxPerRoute(50);
        return HttpClients.custom().setConnectionManager(cm).build();
    }

}
