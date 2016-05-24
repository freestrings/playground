package fs;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class HelloProducer {

    static class ProducerRunner extends TimerTask {

        private AtomicInteger count = new AtomicInteger(0);
        private final KafkaProducer<Object, Object> producer;

        public ProducerRunner() {
            Properties props = new Properties();
            props.put("bootstrap.servers", "localhost:9092");
            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            producer = new KafkaProducer<>(props);
        }

        @Override
        public void run() {
            count.getAndIncrement();
            producer.send(new ProducerRecord<>("my-topics", Integer.toString(count.get()), "value"));
            System.out.printf("Sent: %d%n", count.get());
        }

        public void shutdown() {
            producer.close();
        }
    }

    public static void main(String... args) {

        Timer timer = new Timer();
        ProducerRunner producerRunner = new ProducerRunner();
        timer.schedule(producerRunner, 0, 3000);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                timer.cancel();
                producerRunner.shutdown();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });


    }
}
