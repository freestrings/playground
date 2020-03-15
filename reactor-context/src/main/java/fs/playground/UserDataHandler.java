package fs.playground;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class UserDataHandler {

    public Mono<String> handle(Optional<UserData> userData, MandatoryUserData appData) {
        if (userData.isPresent()) {
            return Mono.just(String.format("name=%s,age=%s,param=%s", userData.get().getName(), userData.get().getAge(), appData.getData()));
        } else {
            return Mono.error(new IllegalArgumentException("user data is null"));
        }
    }

    public Mono<String> handle(Optional<UserData> userData, OptionalUserData appData) {
        if (userData.isPresent()) {
            return Mono.just(String.format("name=%s,age=%s,param-option=%s", userData.get().getName(), userData.get().getAge(), appData.getData()));
        } else {
            return Mono.just("anonymous, param= " + appData.getData());
        }
    }

    public Mono<String> handle(Optional<UserData> userData, AppData appData) {
        if (appData instanceof MandatoryUserData) {
            return handle(userData, (MandatoryUserData) appData);
        }

        if (appData instanceof OptionalUserData) {
            return handle(userData, (OptionalUserData) appData);
        }

        return Mono.error(new IllegalArgumentException());
    }
}
