package fs.playground;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.publisher.*;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

public class MonoApis {

    static Logger log = LoggerFactory.getLogger(ReactorApplication.class);

    <T> void processSink(MonoSink<T> sink, T value, int delay) {
        sink.onRequest(l -> log.info("#request: " + value));
        sink.onCancel(() -> log.info("#cancel: " + value));
        sink.onDispose(() -> log.info("#dispose: " + value));
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }
        }
        sink.success(value);
    }

    <T> Mono<Resp> mono(T value, int delay) {
        WebClient client = WebClient.create("http://localhost:8080");
        return client.get()
                .uri(String.format("/?value=%s&delay=%s", value, delay))
                .retrieve()
                .bodyToMono(Resp.class).log();
    }

    <T> Mono<T> mono(T value, int delay, boolean isAsync) {
        Consumer<MonoSink<T>> syncSink = sink -> processSink(sink, value, delay);
        Consumer<MonoSink<T>> asyncSink = sink -> new Thread(() -> processSink(sink, value, delay)).start();
        return Mono.create(isAsync ? asyncSink : syncSink).log();
    }

    void monoAnd() {
        mono("A", 30, false)
                .and(mono("B", 10, false))
                .and(mono("C", 10, false))
                .subscribe();

        mono("a", 30, true)
                .and(mono("b", 10, true))
                .and(mono("c", 10, true))
                .subscribe();
    }

    void block() {
        log.info(mono("a", 3000, true).block());
        log.info(mono("b", 3000, true).block());
    }

    void cache() {
        Mono<String> a = mono("a", 3000, true).cache();
        a.subscribe(log::info);
        a.subscribe(log::info);
    }

    void cancel() {
        mono("a", 5000, true)
                .cancelOn(Schedulers.boundedElastic())
//                .timeout(Duration.ofMillis(1000))
                .timeout(Duration.ofMillis(1000), Mono.just("b"))
                .subscribe(log::info)
        ;
    }

    void cast() {
//        mono("100", 0, false)
//                .cast(Integer.class)
//                .subscribe(value -> log.info(value.toString()))
//        ;

        mono("100", 0, false)
                .cast(CharSequence.class)
                .subscribe(value -> log.info(value.toString()))
        ;
    }

    void checkpoint() {
        mono("a", 0, false)
                .cast(Integer.class)
                .checkpoint()
                .doOnError(throwable -> log.error("checkpoint error", throwable))
                .subscribe();
    }

    void concatWith() {
//        mono("a", 0, false)
//                .concatWith(mono("b", 0, false))
//                .subscribe(log::info)
//        ;

        mono("a", 3000, true)
                .concatWith(mono("b", 100, true))
                .subscribe(s -> log.info(s.toString()))
        ;
    }

    String a = "a";

    void defer() {
        Mono<String> monoJust = Mono.just(a).log();
        Mono<String> monoDefer = Mono.defer(() -> Mono.just(a).log()).log();

        monoJust.subscribe(log::info);
        monoDefer.subscribe(log::info);

        a = "b";
        monoJust.subscribe(log::info);
        monoDefer.subscribe(log::info);

        monoJust = Mono.<String>create(sink -> {
            log.info("call monoJust");
            sink.success(a);
        }).log();

        monoJust.subscribe(log::info);

        a = "c";
        monoJust.subscribe(log::info);

        Mono<String> mono = mono(a, 0, false);
        mono.subscribe(log::info);
        a = "d";
        mono.subscribe(log::info);

//        Mono<Resp> mono = mono(a, 1000);
//        mono.subscribe(s -> log.info(s.toString()));
//        mono.subscribe(s -> log.info(s.toString()));
    }

    void materalize() {
        Consumer<Signal<?>> siglnalConsumer = signal -> log.info("value: {}, context: {}, throwable: {}, type: {}"
                , signal.get()
                , signal.getContext()
                , signal.getThrowable()
                , signal.getType());

        Mono.just("a")
                .log()
                .materialize()
                .subscribe(siglnalConsumer);

        Mono.error(new Error("a"))
                .log()
                .materialize()
                .subscribe(siglnalConsumer);
    }

    void demateralize() {
        Mono.just("a")
                .log()
                .materialize()
                .<String>dematerialize()
                .subscribe(log::info);
    }

    void doAfterTerminate() {
        Mono.just("a")
                .log()
                .doAfterTerminate(() -> log.info("terminated-1"))
                .subscribe(log::info)
        ;

        Mono.error(new Error("a"))
                .log()
                .doAfterTerminate(() -> log.info("terminated-2"))
//                .doOnError(throwable -> log.error("terminated-error-2", throwable))
                .subscribe(s -> log.info(s.toString()))
        ;

        Mono.just("a")
                .log()
                .doAfterTerminate(() -> log.info("terminated-3"))
                .and(Mono.error(new Error("a"))
                        .log()
                        .doAfterTerminate(() -> log.info("terminated-3-1"))
                        .doOnError(throwable -> log.error("terminated-error-3-1", throwable))
                )
                .subscribe()
        ;

        Mono.just("a")
                .log()
                .cast(Integer.class)
                .doAfterTerminate(() -> log.info("terminated-4"))
                .doOnError(throwable -> log.error("terminated-error-4", throwable))
                .subscribe(i -> log.info(i.toString()))
        ;
    }

    void doFinally() {
        Mono.just("a")
                .log()
                .doFinally(signalType -> log.info("terminated-1, {}", signalType))
                .subscribe(log::info)
        ;

        Mono.error(new Error("a"))
                .log()
                .doFinally(signalType -> log.info("terminated-2, {}", signalType))
                .subscribe(s -> log.info(s.toString()))
        ;

        Mono.just("a")
                .log()
                .doFinally(signalType -> log.info("terminated-3, {}", signalType))
                .and(Mono.error(new Error("a")))
                .subscribe(s -> log.info(s.toString()))
        ;
    }

    void doFirst() {
        Disposable disposable = Mono.just("a")
                .log()
                .doFirst(() -> {
                    log.info("do first 1");
                })
                .doFirst(() -> {
                    log.info("do first 2");
                })
//                .publishOn(Schedulers.boundedElastic())
                .map(s -> {
                    log.info("in map");
                    return s.toUpperCase();
                })
                .doFirst(() -> {
                    log.info("do first 3");
                })
//                .publishOn(Schedulers.boundedElastic())
                .subscribe(log::info);

//        while (!disposable.isDisposed()) {
//            try {
//                Thread.sleep(1);
//            } catch (InterruptedException e) {
//            }
//        }
    }

    void doOnDiscard() {
        Mono.just(1)
                .log()
                .filter(i -> i != 1)
                .doOnDiscard(Integer.class, i -> log.info("discard: {}", i.toString()))
                .subscribe(i -> log.info("subscribe: {}", i.toString()));
    }

    void elapsed() {
        mono("a", 3000, true)
                .elapsed()
                .flatMap((Function<Tuple2<Long, String>, Mono<String>>) objects -> {
                    log.info("elapsed: {}", objects.getT1());
                    return Mono.just(objects.getT2());
                })
                .subscribe(log::info)
        ;
    }

    void expand() {
        Mono.just(5)
                .expand(v -> {
                    log.info("expand: {}", v);
                    return v == 10 ? Flux.empty() : Flux.just((v + 1));
                })
                .subscribe(i -> log.info("subscribe: {}", i));
    }
}
