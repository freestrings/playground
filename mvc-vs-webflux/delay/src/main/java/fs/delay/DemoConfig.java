package fs.delay;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Configuration
public class DemoConfig {

    @Bean
    public RouterFunction<ServerResponse> route() {
        return RouterFunctions.route()
                .GET("/", this::all)
                .build();
    }

    private Mono<ServerResponse> all(ServerRequest request) {
        String delay = request.queryParam("delay").orElse("0");
        String time = request.queryParam("time").orElse("0");
        return Mono.just(time)
                .delayElement(Duration.ofMillis(Long.parseLong(delay)))
                .flatMap(t -> {
                    System.out.println("sent: " + t);
                    return ServerResponse.ok().bodyValue(t);
                });
    }
}
