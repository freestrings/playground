package fs.playground;

import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Optional;

public class UserData {

    public static final String USER_DATA = "user_data";

    private String name;
    private Integer age;

    public static UserData of(ServerRequest request) {
        MultiValueMap<String, String> queryParams = request.queryParams();
        UserData userData = new UserData();
        request.queryParam("name").ifPresent(s -> userData.name = s);
        request.queryParam("age").ifPresent(s -> userData.age = Integer.valueOf(s));
        return userData;
    }

    public Context toContext() {
        if (this.name == null || this.age == null) {
            return Context.empty();
        }
        return Context.of(USER_DATA, this);
    }

    public static Mono<Optional<UserData>> get() {
        return Mono.subscriberContext().map(context -> {
            return context.hasKey(USER_DATA) ? Optional.of(context.get(USER_DATA)) : Optional.empty();
        });
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
