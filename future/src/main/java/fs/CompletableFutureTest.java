package fs;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class CompletableFutureTest {

    public static void main(String... args) {

        CompletableFuture futureForAcceptEither = new CompletableFuture();

        CompletableFuture future = new CompletableFuture();
        future.handle(new BiFunction() {
            @Override
            public Object apply(Object o, Object o2/*Exception*/) {
                System.out.println(String.format("handle1: %s, %s", o, o2));
                return o;
            }
        }).handle(new BiFunction() {
            @Override
            public Object apply(Object o, Object o2) {
                System.out.println(String.format("handle2: %s, %s", o, o2));
                return o;
            }
        }).acceptEither(futureForAcceptEither, o -> {
            System.out.println(String.format("acceptEither: %s", o));
        }).thenAccept(new Consumer() {
            @Override
            public void accept(Object o) {
                System.out.println(String.format("thenAccept2: %s", o));
            }
        }).thenAccept(new Consumer() {
            @Override
            public void accept(Object o) {
                System.out.println(String.format("thenAccept1: %s", o));
            }
        }).whenComplete(new BiConsumer() {
            @Override
            public void accept(Object o, Object o2) {
                System.out.println(String.format("whenComplete1: %s, %s", o, o2));
                throw new RuntimeException("aaa");
            }
        }).whenComplete(new BiConsumer() {
            @Override
            public void accept(Object o, Object o2) {
                System.out.println(String.format("whenComplete2: %s, %s", o, o2));
            }
        }).exceptionally(new Function() {
            @Override
            public Object apply(Object o) {
                System.out.println(String.format("exceptionally: %s", o));
                return new Object();
            }
        });

//        futureForAcceptEither.completeExceptionally(new Exception("??"));
        future.complete("TEST");
//        future.completeExceptionally(new Exception());

    }
}
