package fs.playground;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
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

    void concatMap() {
        Flux.just("a", "b")
                .log()
//                .parallel()
//                .runOn(Schedulers.boundedElastic())
                .concatMap((Function<String, Publisher<?>>) s -> s.equals("a") ? Mono.just(s) : Flux.just(s, "c"))
                .reduce((o1, o2) -> {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    if (o1 instanceof Map) {
                        hashMap.putAll((Map) o1);
                    } else {
                        hashMap.put(UUID.randomUUID().toString(), o1);
                    }
                    if (o2 instanceof Map) {
                        hashMap.putAll((Map) o2);
                    } else {
                        hashMap.put(UUID.randomUUID().toString(), o2);
                    }
                    return hashMap;
                })
                .subscribe(s -> log.info("{}", s))
        ;
    }

    void flatMap() throws InterruptedException {
//        Disposable disposable = Flux.just("a", "b", "c")
//                .log()
//                .flatMap(value -> Mono.just(value.toUpperCase()).subscribeOn(Schedulers.boundedElastic()))
////                .flatMap(value -> Mono.just(value.toUpperCase()).subscribeOn(Schedulers.boundedElastic()), 10)
////                .flatMap(value -> Mono.just(value.toUpperCase()).subscribeOn(Schedulers.boundedElastic()), 1)
//                .subscribe(log::info);
//
//        while (!disposable.isDisposed()) {
//            Thread.sleep(1);
//        }

//        Flux.just("a", "b", "c", "d", "e", "f")
//                .log()
//                .parallel()
//                .runOn(Schedulers.boundedElastic())
//                .flatMap(value -> {
//                    log.info("flatMap: {}", value);
//                    return Mono.just(value.toUpperCase());
//                })
//                .subscribe(log::info);

//        Flux.just("a", "b", "c", "d", "e", "f")
//                .log()
//                .flatMapSequential(value -> Mono.just(value.toUpperCase()))
//                .parallel()
//                .runOn(Schedulers.boundedElastic())
//                .subscribe(log::info);
    }

    void limitRate() {
//        Flux.just("a", "b", "c", "d", "e", "f")
//                .log()
//                .limitRate(2)
//                .subscribe(log::info);

        Flux.just("a", "b", "c", "d", "e", "f")
                .log()
                .publishOn(Schedulers.elastic(), 2)
                .subscribe(log::info);
    }

    void merge() {
//        Flux.merge(Mono.just("a"), Mono.just(1), Mono.just("b"))
//                .log()
//                .buffer()
//                .parallel()
//                .runOn(Schedulers.boundedElastic())
//                .subscribe(s -> {
//                    log.info("{}", s);
//                })
//        ;

        Flux.merge(Mono.just("a"), Mono.just(new Object()), Mono.just(1))
                .log()
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .reduce((first, second) -> {
                    if (second instanceof Map) {
                        if (first instanceof Map) {
                            ((Map) second).putAll((Map) first);
                        } else {
                            ((Map) second).put(UUID.randomUUID().toString(), first);
                        }
                        return second;
                    } else {
                        Map<String, Object> a = new HashMap<>();
                        a.put(UUID.randomUUID().toString(), first);
                        a.put(UUID.randomUUID().toString(), second);
                        return a;
                    }
                })
                .subscribe(s -> {
                    log.info("{}", s);
                })
        ;

//        Flux.merge(Mono.just("a"), Mono.just(new Object()), Mono.just(1))
//                .log()
//                .buffer()
//                .parallel()
//                .runOn(Schedulers.boundedElastic())
//                .reduce((first, second) -> {
//                    if (second instanceof Map) {
//                        if (first instanceof Map) {
//                            ((Map) second).putAll((Map) first);
//                        } else {
//                            ((Map) second).put(UUID.randomUUID().toString(), first);
//                        }
//                        return second;
//                    } else {
//                        Map<String, Object> a = new HashMap<>();
//                        a.put(UUID.randomUUID().toString(), first);
//                        a.put(UUID.randomUUID().toString(), second);
//                        return a;
//                    }
//                })
//                .subscribe(s -> {
//                    log.info("{}", s);
//                })
//        ;
    }

    void zip() {
        Flux.zip(Mono.just("a"), Mono.just("b"))
                .log()
                .subscribe(s -> log.info("{}", s));

        Flux.zip(Mono.just("a"), Mono.just("b"), Flux.just("c", "d"))
                .log()
                .subscribe(s -> log.info("{}", s));

        Flux.zip(Flux.just("a", "b"), Flux.just("c", "d"), Flux.just("e", "f"))
                .log()
                .subscribe(s -> log.info("{}", s));
    }
}
