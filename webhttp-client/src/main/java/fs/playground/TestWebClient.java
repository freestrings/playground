package fs.playground;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.stream.Collectors;

public class TestWebClient implements Testable {

    @Override
    public void doTest(int loop, String url) {
        WebClient webClient = WebClient.builder().baseUrl(url).build();
        Flux.range(0, loop)
                .flatMap(v ->
                        webClient
                                .get()
                                .uri("?" + System.currentTimeMillis())

                                .retrieve()
                                .bodyToMono(String.class)
                )
                .collect(Collectors.joining()).block();
    }
}
