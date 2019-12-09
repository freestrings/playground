package fs.playground;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.stream.Collectors;

public class FluxApis {
    static Logger log = LoggerFactory.getLogger(FluxApis.class);

    void all() {
        Flux.just("a", "b")
                .log()
                .all(s -> !s.isEmpty())
                .subscribe(s -> log.info("{}", s))
        ;

        Flux.just("a", "")
                .log()
                .all(s -> !s.isEmpty())
                .subscribe(s -> log.info("{}", s))
        ;
    }

    void buffer() throws InterruptedException {
        Flux.just(
                Mono.just("a").delayElement(Duration.ofMillis(100)),
                Mono.just("b").delayElement(Duration.ofMillis(1000))
        ).log()
                .buffer()
                .subscribe(s -> log.info("{}", s));

        Flux.just("a", "b")
                .log()
                .buffer()
                .subscribe(s -> log.info("{}", s));

        Disposable disposable = Flux.concat(
                Mono.just("a").delayElement(Duration.ofMillis(1000)),
                Mono.just("b").delayElement(Duration.ofMillis(3000))
        ).log()
                .buffer()
                .subscribe(s -> log.info("{}", s));

        while (!disposable.isDisposed()) {
            Thread.sleep(1);
        }
    }

    void collect() throws InterruptedException {
        Flux.just(
                Mono.just("a").delayElement(Duration.ofMillis(100)),
                Mono.just("b").delayElement(Duration.ofMillis(1000))
        ).log()
                .collect(Collectors.toList())
                .subscribe(s -> log.info("{}", s));

        Disposable disposable = Flux.concat(
                Mono.just("a").delayElement(Duration.ofMillis(1000)),
                Mono.just("b").delayElement(Duration.ofMillis(3000))
        ).log()
                .collect(Collectors.toList())
                .subscribe(s -> log.info("{}", s));

        while (!disposable.isDisposed()) {
            Thread.sleep(1);
        }
    }

    // https://www.baeldung.com/reactor-combine-streams
    void combineLatest() {
        Flux.combineLatest(
                Flux.just("a", "A", "1"),
                Flux.just("b", "B", "2"),
                (s, s2) -> s + s2
        ).log()
                .subscribe(log::info);
    }
}
