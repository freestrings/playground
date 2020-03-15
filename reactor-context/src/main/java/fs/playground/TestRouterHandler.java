package fs.playground;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class TestRouterHandler {

    private final UserDataCombinator userDataCombinator;

    public TestRouterHandler(UserDataCombinator userDataCombinator) {
        this.userDataCombinator = userDataCombinator;
    }

    public Mono<String> param(ServerRequest request) {
        String param = request.queryParam("param").get();
        return Mono.just(param);
    }

    public Mono<ServerResponse> string(ServerRequest request) {
        return param(request)
                .map(p -> new MandatoryUserData(p))
                .publish(v -> userDataCombinator.handle(v))
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    public Mono<ServerResponse> number(ServerRequest request) {
        return param(request)
                .map(p -> new OptionalUserData(Integer.valueOf(p)))
                .publish(v -> userDataCombinator.handle(v))
                .flatMap(ServerResponse.ok()::bodyValue);
    }
}
