package fs;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class CompletableFutureChainTest {

    public static void main(String... args) {

        CompletableFuture rootFuture = new CompletableFuture();
        CompletableFuture future1 = rootFuture.thenApply(new Function() {
            @Override
            public Object apply(Object o) {
                System.out.println("A");
                if (false) throw new RuntimeException("error");
                CompletableFuture<Object> f1 = new CompletableFuture<>();
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    f1.complete(1);
                }).start();
                return f1;
            }
        });

        CompletableFuture future2 = future1.thenApply(new Function<CompletableFuture, Object>() {
            @Override
            public Object apply(CompletableFuture o) {
                System.out.println("B");
                CompletableFuture<Object> f1 = new CompletableFuture<>();
                o.whenComplete(new BiConsumer() {
                    @Override
                    public void accept(Object o, Object o2) {
                        System.out.println(o);
                        new Thread(() -> {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                            }
                            f1.complete(2);
                        }).start();
                    }
                });

                return f1;
            }
        });

        CompletableFuture future3 = future2.thenApply(new Function<CompletableFuture, Object>() {
            @Override
            public Object apply(CompletableFuture o) {
                o.whenComplete(new BiConsumer() {
                    @Override
                    public void accept(Object o, Object o2) {
                        System.out.println(o);
                    }
                });
                return null;
            }
        });

        future3.exceptionally(new Function() {
            @Override
            public Object apply(Object o) {
                System.out.println("exception");
                return null;
            }
        });

        rootFuture.complete("1");

    }
}
