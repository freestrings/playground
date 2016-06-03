package fs.producer.config;

import fs.producer.ProducerRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LingerMsBathcSize {

    public static void main(String... args) {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        //
        // linger.ms 만큼 지연 후 메시지를 보냄.
        //
        final ProducerRunner producerRunner1 = new ProducerRunner(
                new Object[]{"linger.ms", Integer.toString(3000)}
        );

        //
        // batch.size 때문에 메시지를 즉시 보냄.
        //
        final ProducerRunner producerRunner2 = new ProducerRunner(
                new Object[]{"linger.ms", Integer.toString(3000)},
                new Object[]{"batch.size", 1}
        );

        executor.submit((Runnable) () -> System.out.println(producerRunner1.send()));
        executor.submit((Runnable) () -> System.out.println(producerRunner2.send()));

        ProducerRunner.awaiteTerminate(executor);
    }

}
