package fs;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class HelloConsumer {

    static class ConsumerRunner implements Runnable {

        private AtomicBoolean closed = new AtomicBoolean(false);

        private final KafkaConsumer<Object, Object> consumer;

        public ConsumerRunner() {
            Properties props = new Properties();
            props.put("bootstrap.servers", "localhost:9092");
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("group.id", "my-consumer");
            this.consumer = new KafkaConsumer<>(props);
        }

        @Override
        public void run() {

            this.consumer.subscribe(Arrays.asList("my-topics"));

            try {
                while (!this.closed.get()) {
                    this.consumer.poll(100)
                            .forEach(record ->
                                    System.out.printf("(%s) - %s : %s%n", record.topic(), record.key(), record.value())
                            );
                }
            } catch (WakeupException e) {

            } finally {
                this.consumer.close();
            }
        }

        public void shutdown() {
            this.closed.set(true);
            this.consumer.wakeup();
        }
    }

    public static void main(String... args) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        ConsumerRunner consumerRunner = new ConsumerRunner();
        executor.submit(consumerRunner);

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                consumerRunner.shutdown();

                try {
                    executor.awaitTermination(3000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
