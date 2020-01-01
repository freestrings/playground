package fs.playground;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@ContextConfiguration(classes = {RouteConfig.class})
@WebFluxTest
public class RouterTests {

    private WebTestClient webTestClient;

    @Autowired
    private ApplicationContext context;

    @BeforeEach
    public void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(context).build();
    }

    @Test
    public void get() {
        webTestClient.get()
                .uri("/")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value((Consumer<String>) s -> Assertions.assertEquals(s, "get"));
    }

    @Test
    public void post() {
        webTestClient.post()
                .uri("/")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new HashMap<>())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .value((Consumer<Map>) s -> Assertions.assertEquals(s, new HashMap<>()));
    }

    @Test
    public void postError() {
        webTestClient.post()
                .uri("/")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new HashMap<>() {{
                    put("error", true);
                }})
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .value((Consumer<String>) s -> Assertions.assertEquals(s, "force error"));
    }
}
