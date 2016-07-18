package fs.producer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiProducer {

    public static void main(String... args) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        final ProducerRunner producerRunner1 = new ProducerRunner();
        final ProducerRunner producerRunner2 = new ProducerRunner();

        executor.submit((Runnable) () -> System.out.println(producerRunner1.send()));
        executor.submit((Runnable) () -> System.out.println(producerRunner2.send()));

        ProducerRunner.awaiteTerminate(executor);
    }
}
