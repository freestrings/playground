package hello;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQuery;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootApplication
public class CouchbaseTest implements CommandLineRunner {

    public static void main(String... args) {
        SpringApplication.run(CouchbaseTest.class);
    }

    @Autowired
    CouchbaseRepository repository;

    @Autowired
    CouchbaseTemplate couchbaseTemplate;

    @Override
    public void run(String... args) throws Exception {
//        List<Company> companies = IntStream.range(0, 1000)
//                .mapToObj(i -> new Company("Company" + i))
//                .collect(Collectors.toList());
//
//        Customer customer = new Customer("name", "message", companies);
//        repository.save(customer);
//        System.out.println("Start");
//        new Thread(() -> {
//            long l = System.currentTimeMillis();
//            for (int i = 0; i < 10000; i++) {
//                Optional<Customer> name = repository.findById("name");
//                Customer customer = name.get();
//                Customer customer = couchbaseTemplate.findById("name", Customer.class);
//                Assert.isTrue(customer.getName().equals("name"), "");
//            }
//            System.out.println(System.currentTimeMillis() - l);
//        }).start();

        Cluster cluster = CouchbaseCluster.create();
        Bucket customerBucket = cluster.openBucket("customer");

//        JsonArray companies = JsonArray.empty();
//        for (int i = 0; i < 1000; i++) {
//            JsonObject companyName = JsonObject.empty().put("companyName", "company" + i);
//            companies.add(companyName);
//        }
//        JsonObject user = JsonObject.empty()
//                .put("name", "name")
//                .put("message", "message")
//                .put("compines", companies);
//        customerBucket.upsert(JsonDocument.create("name", user));
        System.out.println("Start");
        long l = System.currentTimeMillis();
        Gson gson = new Gson();
        for (int i = 0; i < 10000; i++) {
            customerBucket.query(N1qlQuery.simple("update customer " +
                    "use keys \"name\" " +
                    "set c.companyName=\"ccc " + i + " \" for i : c in compines when i = 1 end"));
//            JsonDocument doc = customerBucket.get("name");
//            Customer customer = gson.fromJson(doc.content().toString(), Customer.class);
//            Assert.isTrue(customer.getName().equals("name"), "");
        }
        System.out.println(System.currentTimeMillis() - l);
    }
}
