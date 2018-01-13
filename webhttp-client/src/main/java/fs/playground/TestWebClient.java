package fs.playground;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

public class TestWebClient implements Testable {

    private String toBaseUrl(String strUrl) {
        try {
            URL url = new URL(strUrl);
            return String.format("%s://%s:%d", url.getProtocol(), url.getHost(), url.getPort());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String toPath(String strUrl) {
        try {
            return new URL(strUrl).getPath();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void doTest(int loop, String url) {
        WebClient webClient = WebClient.builder().baseUrl(toBaseUrl(url)).build();
        String path = toPath(url);
        Flux.range(0, loop)
                .flatMap(v ->
                        webClient
                                .get()
                                .uri(path + "?" + System.currentTimeMillis())

                                .retrieve()
                                .bodyToMono(String.class)
                )
                .collect(Collectors.joining()).block();
    }
}
