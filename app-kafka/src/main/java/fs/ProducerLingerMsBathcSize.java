package fs;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ProducerLingerMsBathcSize {

    static DateTimeFormatter DTF = DateTimeFormat.forPattern("k시m분s초");

    static class ProducerRunner implements Runnable {

        private final KafkaProducer<String, String> producer;
        private final Map<String, String> addtionalProp;

        public ProducerRunner(Map<String, String> additionalProp) {
            Properties props = new Properties();
            props.put("bootstrap.servers", "localhost:9092");
            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            this.addtionalProp = additionalProp;
            props.putAll(additionalProp);
            this.producer = new KafkaProducer<>(props);
        }

        @Override
        public void run() {

            long start = System.currentTimeMillis();
            producer.send(
                    new ProducerRecord<>("my-topics", DTF.print(start), this.addtionalProp.keySet().toString()),
                    (metadata, exception) ->
                            System.out.printf(
                                    "걸린시간: %dms, %s%n",
                                    System.currentTimeMillis() - start,
                                    this.addtionalProp.keySet().toString()
                            )
            );
            System.out.printf("보냄: %s, %s%n", DTF.print(start), this.addtionalProp.keySet().toString());
        }

        public void shutdown() {
            producer.close();
        }
    }

    public static void main(String... args) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<ProducerRunner> producerRunners = Arrays.asList(
                getLingerMsOnlyProducerRunner(),
                getLingerMsWithBatchSizeProducerRunner()
        );
        producerRunners.forEach(producerRunner -> executor.submit(producerRunner));

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                producerRunners.forEach(producerRunner -> producerRunner.shutdown());

                try {
                    executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    /**
     * linger.ms 만큼 지연 후 메시지를 보냄.
     */
    private static ProducerRunner getLingerMsOnlyProducerRunner() {
        HashMap<String, String> map = new HashMap<>();
        map.put("linger.ms", Integer.toString(3000));
        return new ProducerRunner(map);
    }

    /**
     * batch.size 때문에 메시지를 즉시 보냄.
     */
    private static ProducerRunner getLingerMsWithBatchSizeProducerRunner() {
        HashMap<String, String> map = new HashMap<>();
        map.put("linger.ms", Integer.toString(3000));
        map.put("batch.size", Integer.toString(1));
        return new ProducerRunner(map);
    }
}
