package fs.webflux;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
public class WebFluxConfig {

    private final HttpClient httpClient = HttpClient.create()
            .tcpConfiguration(tcpClient -> {
                return tcpClient
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                        .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(3000, TimeUnit.MILLISECONDS)));
            });

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://delay:8080")
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();

    @Bean
    public RouterFunction<ServerResponse> route() {
        return RouterFunctions.route()
                .GET("/multi", this::multi)
                .GET("/http", this::http)
                .GET("/blocking", this::blocking)
                .build();
    }

    private Mono<ClientResponse> httpCall() {
        return webClient.get().uri("/?delay=20000&time=" + System.currentTimeMillis()).exchange();
    }

    public Mono<ServerResponse> multi(ServerRequest request) {
        String v = request.queryParam("v").get();
        System.out.println(Thread.currentThread() + " - " + v);
        return Mono.zip(httpCall(), httpCall())
                .flatMap(pair -> {
                    return pair.getT1().bodyToMono(String.class)
                            .zipWith(pair.getT2().bodyToMono(String.class))
                            .map(echoTimes -> {
                                return (Long.parseLong(echoTimes.getT1()) + Long.parseLong(echoTimes.getT2())) / 2;
                            });
                })
                .flatMap(echoTime -> {
                    System.out.println(Thread.currentThread() +
                            " = " +
                            v +
                            " - " +
                            (System.currentTimeMillis() - echoTime) // 걸린시간
                    );
                    return ServerResponse.ok().bodyValue(v);
                });
    }

    public Mono<ServerResponse> http(ServerRequest request) {
        String v = request.queryParam("v").get();
        System.out.println(Thread.currentThread() + " < " + v);
        return httpCall().flatMap(response -> {
            return response.bodyToMono(String.class).flatMap(s -> {
                System.out.println(Thread.currentThread() + " > " +
                        v +
                        " - " +
                        (System.currentTimeMillis() - Long.parseLong(s)) // 걸린시간
                );
                return ok().bodyValue(v);
            });
        });
    }

    public Mono<ServerResponse> blocking(ServerRequest request) {
        String v = request.queryParam("v").get();
        System.out.println(Thread.currentThread() + " < " + v);
        return Mono.fromCallable(() -> {
            Thread.sleep(300);
            System.out.println(Thread.currentThread() + " > " + v);
            return null;
        }).then(ok().bodyValue(v));
    }

}

