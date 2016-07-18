package fs.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ProducerRunner {

    protected static DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormat.forPattern("k시m분s초");
    protected final Map<String, Object> addtionalProperties;
    protected final KafkaProducer<String, String> producer;

    public ProducerRunner() {
        this(new Object[]{});
    }

    public ProducerRunner(Object[]... properties) {
        Map<String, Object> props = new HashMap();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        this.addtionalProperties = Collections.unmodifiableMap(convertToMap(properties));
        props.putAll(this.addtionalProperties);
        this.producer = new KafkaProducer<>(props);

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                ProducerRunner.this.shutdown();
            }

        });
    }

    private HashMap<String, Object> convertToMap(Object[][] properties) {
        return Arrays.asList(properties).stream()
                .reduce(new HashMap<>(), (map, objects) -> {
                    map.put(objects[0].toString(), objects[1]);
                    return map;
                }, (map1, map2) -> {
                    map1.putAll(map2);
                    return map1;
                });
    }

    public void shutdown() {
        this.producer.close();
    }

    public String send() {
        return this.send("my-topic");
    }

    public String send(String topic) {
        long start = System.currentTimeMillis();
        ProducerRecord<String, String> record = new ProducerRecord<>(
                topic,
                DATE_TIME_FORMAT.print(start),
                this.addtionalProperties.keySet().toString()
        );

        this.producer.send(record, (metadata, exception) ->
                System.out.printf(
                        "걸린시간: %dms, %s%n",
                        System.currentTimeMillis() - start,
                        this.addtionalProperties.keySet().toString()
                ));

        return String.format("보냄: %s, %s%n",
                DATE_TIME_FORMAT.print(start),
                this.addtionalProperties.keySet().toString());
    }

    public static void awaiteTerminate(ExecutorService executor) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
