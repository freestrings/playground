package fs.playground;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UserDataCombinator<I extends AppData> {

    private final UserDataHandler userDataHandler;

    public UserDataCombinator(UserDataHandler userDataHandler) {
        this.userDataHandler = userDataHandler;
    }

    public Mono<String> handle(Mono<I> actual) {
        return UserData.get()
                .zipWith(actual)
                .flatMap(pair -> {
                    return userDataHandler.handle(pair.getT1(), pair.getT2());
                });
    }
}
