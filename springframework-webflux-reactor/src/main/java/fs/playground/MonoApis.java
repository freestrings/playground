package fs.playground;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.Scannable;
import reactor.core.publisher.*;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class MonoApis {

    static Logger log = LoggerFactory.getLogger(ReactorApplication.class);
    String a = "a";

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

    <T> Mono<Void> err(int delay) {
        WebClient client = WebClient.create("http://localhost:8080/err");
        return client.get()
                .uri(String.format("/?delay=%s", delay))
                .retrieve()
                .bodyToMono(Void.class)
                .log();
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

    void first() {
        Mono.first(
                mono("a", 3000, true)
                , mono("b", 1000, true)
        ).subscribe(log::info);
    }

    // https://stackoverflow.com/questions/49115135/map-vs-flatmap-in-reactor
    void flatMap() throws InterruptedException {
        Mono.just("a")
                .flatMap(i -> mono(i, 3000, true))
                .subscribe(log::info);
    }

    // a를 cancel시키고 (b는 실행 안됨) from 으로 갈아탐
    void from() {
        Mono.from(
                Flux.concat(
                        Mono.just("a").log("log-a"),
                        Mono.just("b").log("log-b")
                ).log()
        ).log("log-from").subscribe(log::info);
    }

//    void fromCompletionStage2() throws Exception {
//        Mono.fromCompletionStage(err(1).toFuture())
//                .log()
//                .doFinally(signalType -> log.info("finally: {}", signalType))
//                .subscribe(i -> log.info("subscribe: {}", i));
//    }

    // ex) https://github.com/lettuce-io/lettuce-core/blob/master/src/main/java/io/lettuce/core/masterslave/AutodiscoveryConnector.java#L58
    // SignalType.CANCEL 안옴??
    // - fromFuture
    void fromCompletionStage() throws Exception {
        CompletableFuture<Long> future = Mono.delay(Duration.ofSeconds(3)).log().take(Duration.ofSeconds(1)).toFuture();
//        future.cancel(false);
        Disposable disposable = Mono.fromCompletionStage(future)
                .log()
                .doFinally(signalType -> log.info("finally: {}", signalType))
                .subscribe(i -> log.info("subscribe: {}", i));

        while (!disposable.isDisposed()) {
            Thread.sleep(1);
        }
    }

    void handle() {
        Mono.just("a").log()
                .handle((BiConsumer<String, SynchronousSink<String>>) (s, synchronousSink) -> synchronousSink.next("b"))
                .subscribe(log::info)
        ;

        Mono.just("c").log()
                .handle((BiConsumer<String, SynchronousSink<String>>) (s, synchronousSink) -> synchronousSink.error(new Exception("d")))
                .doOnError(e -> log.error("exception", e))
                .subscribe(log::info)
        ;
    }

    // synchronous? ~= return type is not Mono or Flux
    void mapAndFlatMap() {
        Mono.just(1)
                .log()
                .expand(s -> s == 5 ? Flux.empty() : Flux.just(s + 1))
//                .parallel()
//                .runOn(Schedulers.elastic())
                .map(s -> {
                    log.info("map: {}", s);
                    return s.toString();
                })
                .subscribe(log::info)
        ;

        Mono.just(1)
                .log()
                .expand(s -> s == 5 ? Flux.empty() : Flux.just(s + 1))
//                .parallel()
//                .runOn(Schedulers.elastic())
                .flatMap(s -> {
                    log.info("flatMap: {}", s);
                    return Mono.just(s.toString());
                })
                .subscribe(log::info)
        ;
    }

    //
    //   a ->
    //     -> b -> c ->
    //=> a -> b -> c ->
    void mergeWith() throws InterruptedException {
        Disposable disposable = mono("a", 30, true)
                .mergeWith(mono("b", 10, true))
                .mergeWith(mono("c", 20, true))
                .subscribe(log::info);
        while (!disposable.isDisposed()) {
            Thread.sleep(1);
        }

        log.info("----------------------");

        disposable = mono("a", 10, true)
                .mergeWith(mono("b", 20, true))
                .mergeWith(mono("c", 30, true))
                .subscribe(log::info);
        while (!disposable.isDisposed()) {
            Thread.sleep(1);
        }
    }

    // continuing with subsequent elements.
    void onErrorContinue() {
        Mono.error(new Exception("a"))
                .mergeWith(Mono.just("b"))
                .doOnError(e -> log.error("onError", e))
                .onErrorContinue((e, o) -> log.error(String.format("onErrorContinue: %s", o), e))
                .subscribe(s -> log.info("{}", s));
    }

    // using a function to choose the fallback
    void onErrorResume() {
        Mono.error(new Exception("a"))
                .mergeWith(Mono.just("b"))
                .doOnError(e -> log.error("onError", e))
                .onErrorResume(e -> Mono.just("b"))
                .subscribe(s -> log.info("{}", s));
    }

    void onErrorStop() {
        Mono.error(new Exception("a"))
                .mergeWith(Mono.just("b"))
                .doOnError(e -> log.error("onError", e))
//                .onErrorStop()
                .onErrorContinue((e, o) -> log.error(String.format("onErrorContinue: %s", o), e))
                .subscribe(s -> log.info("{}", s));
    }

    void publish() {
        Mono<String> a = Mono.just("a").log();
        Mono<String> A = Mono.just("A").log();

        a.publish((Function<Mono<String>, Mono<?>>) mono -> A);
        a.publish((Function<Mono<String>, Mono<?>>) mono -> A);
        a.subscribe(s -> log.info("1. {}", s));

        a.publish((Function<Mono<String>, Mono<?>>) mono -> A).subscribe(s -> log.info("2. {}", s));
        a.publish((Function<Mono<String>, Mono<?>>) mono -> A).subscribe(s -> log.info("3. {}", s));
        a.subscribe(s -> log.info("4. {}", s));
    }

    //  if it signals any error
    void retry() {
        Mono.just("a")
                .log()
                .retry(10)
                .subscribe(log::info)
        ;

        Mono.error(new Exception("b"))
                .log()
                .retry(10)
                .doOnError(e -> log.error(e.getMessage()))
                .subscribe()
        ;
    }

    void sequenceEqual() {
        Mono.sequenceEqual(Mono.just("a"), Mono.just("a"))
                .log()
                .subscribe(s -> log.info("{}", s));

        Mono.sequenceEqual(Mono.just("a"), Flux.just("a"))
                .log()
                .subscribe(s -> log.info("{}", s));
    }

    void tag() {
        Mono<String> tag = Mono.just("a")
                .tag("key1", "value1")
                .tag("key2", "value2");

        boolean found = Scannable.from(tag)
                .tags()
                .anyMatch(objects -> objects.getT1().equals("key1"));
        log.info("{}", found);
    }

    void then() {
        Mono.just("a")
                .log()
                .then()
                .mergeWith(Mono.error(new Exception("b")))
//                .doOnError(e -> log.error("doOnError", e))
                .onErrorContinue((e, o) -> {
                })
                .then(Mono.just("c"))
                .subscribe(s -> log.info("subscribe: {}", s))
        ;
    }

    void zip() {
        Mono.zip(Mono.just("a"), Mono.just("b"))
                .subscribe(tuple -> log.info("{} : {}", tuple.getT1(), tuple.getT2()))
        ;
    }
}
