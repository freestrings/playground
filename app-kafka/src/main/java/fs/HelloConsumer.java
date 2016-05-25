package fs;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class HelloConsumer {

    static class ConsumerRunner implements Runnable {

        private String name;
        private AtomicBoolean closed = new AtomicBoolean(false);
        private KafkaConsumer<Object, Object> consumer;

        public ConsumerRunner(String name, ExecutorService executor, List<ConsumerRunner> consumerRunnerList) {
            Properties props = new Properties();
            props.put("bootstrap.servers", "localhost:9092");
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("group.id", "my-consumer");
            this.consumer = new KafkaConsumer<>(props);
            this.name = name;
            consumerRunnerList.add(this);
            executor.submit(this);
        }

        @Override
        public void run() {

            this.consumer.subscribe(Arrays.asList("my-topics"));

            try {
                while (!this.closed.get()) {
                    this.consumer.poll(100)
                            .forEach(record ->
                                    System.out.printf("[%s] - (%s) - %s : %s%n", this.name, record.topic(), record.key(), record.value())
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

        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<ConsumerRunner> consumerList = new ArrayList<>();

        Arrays.asList("A", "B")
                .forEach(ch -> new ConsumerRunner(ch, executor, consumerList));

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                consumerList.forEach(consumer -> consumer.shutdown());

                try {
                    executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
