package hello;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.Properties;

/**
 * https://github.com/wurstmeister/kafka-docker
 * => docker-compose -f docker-compose-single-broker.yml up
 * => docker-compose-single-broker.yml
 * => KAFKA_ADVERTISED_HOST_NAME: 172.20.0.2
 */
@SpringBootApplication
public class KafkaTest implements CommandLineRunner {

    public static void main(String... args) {
        SpringApplication.run(KafkaTest.class, args);
    }

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Override
    public void run(String... args) throws Exception {
        String cmd = args[0];
        if ("create".equals(cmd)) {
            //
            // https://stackoverflow.com/questions/16946778/how-can-we-create-a-topic-in-kafka-from-the-ide-using-api
            //

            String zookeeperConnect = "0.0.0.0:2181";
            int sessionTimeoutMs = 10 * 1000;
            int connectionTimeoutMs = 8 * 1000;

            String topic = "test-topic1";
            int partitions = 2;
            int replication = 1;
            Properties topicConfig = new Properties(); // add per-topic configurations settings here

            // Note: You must initialize the ZkClient with ZKStringSerializer.  If you don't, then
            // createTopic() will only seem to work (it will return without error).  The topic will exist in
            // only ZooKeeper and will be returned when listing topics, but Kafka itself does not create the
            // topic.
            ZkClient zkClient = new ZkClient(
                    zookeeperConnect,
                    sessionTimeoutMs,
                    connectionTimeoutMs,
                    ZKStringSerializer$.MODULE$);

            // Security for Kafka was added in Kafka 0.9.0.0
            boolean isSecureKafkaCluster = false;

            ZkUtils zkUtils = new ZkUtils(zkClient, new ZkConnection(zookeeperConnect), isSecureKafkaCluster);
            AdminUtils.createTopic(zkUtils, topic, partitions, replication, topicConfig, RackAwareMode.Enforced$.MODULE$);
            zkClient.close();
        } else if ("send".equals(cmd)) {
            kafkaTemplate.send("test-topic1", "test").addCallback(new ListenableFutureCallback() {
                @Override
                public void onFailure(Throwable throwable) {
                    System.out.println("Error => ");
                    System.out.println(throwable);
                }

                @Override
                public void onSuccess(Object o) {
                    System.out.println("Success => ");
                    System.out.println(o);
                }
            });
        }
    }
}
