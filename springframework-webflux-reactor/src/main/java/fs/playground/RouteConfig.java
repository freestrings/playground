package fs.playground;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Configuration
public class RouteConfig {

    @Bean
    public RouterFunction<ServerResponse> testRoute() {
        return RouterFunctions.route()
                .GET("/", request -> {
                    Optional<String> delay = request.queryParam("delay");
                    return ServerResponse.ok().bodyValue("get")
                            .delayElement(
                                    delay.isEmpty() ?
                                            Duration.ZERO :
                                            Duration.ofMillis(Long.parseLong(delay.get()))
                            );
                })
                .POST("/", request -> request.bodyToMono(Map.class)
                        .flatMap((Function<Map, Mono<ServerResponse>>) map -> {
                            log.info("input: {}", map);
                            if (map.containsKey("error")) {
                                throw new IllegalStateException("force error");
                            }
                            return ServerResponse.ok().bodyValue(map);
                        })
                        .onErrorResume(throwable -> { // onErrorResume 없으면 DefaultErrorWebExceptionHandler 탐
                            log.info("error message: {}", throwable.getMessage());
                            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(throwable.getMessage());
                        })
                )
                .filter((request, next) -> {
                    log.info("route filter");
                    return next.handle(request);
                })
                .after((request, response) -> {
                    log.info("route after");
                    return response;
                })
                .build()
                ;
    }
}
