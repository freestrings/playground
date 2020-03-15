package fs.playground;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class TestRouterConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(TestRouterHandler testRouterHandler) {
        return route()
                .filter((request, next) -> {
                    return next.handle(request).subscriberContext(UserData.of(request).toContext());
                })
                .GET("/madatory", testRouterHandler::string)
                .GET("/optional", testRouterHandler::number)
                .build();
    }
}
