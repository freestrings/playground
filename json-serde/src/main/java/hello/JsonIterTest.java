package hello;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.jsoniter.JsonIterator;
import com.jsoniter.output.JsonStream;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * ➜  json-serde git:(master) ✗ java -jar target/json-serde-1.0-SNAPSHOT.jar jackson-se
 * ##################Start Jackson Serialize
 * ##################Jackson :967
 * ➜  json-serde git:(master) ✗ java -jar target/json-serde-1.0-SNAPSHOT.jar jackson-se
 * ##################Start Jackson Serialize
 * ##################Jackson :1128
 * ➜  json-serde git:(master) ✗ java -jar target/json-serde-1.0-SNAPSHOT.jar jackson-se
 * ##################Start Jackson Serialize
 * ##################Jackson :1109
 * <p>
 * <p>
 * ➜  json-serde git:(master) ✗ java -jar target/json-serde-1.0-SNAPSHOT.jar jsoniter-se
 * ##################Start JsonIterator Serialize
 * ##################JsonIterator :1353
 * ➜  json-serde git:(master) ✗ java -jar target/json-serde-1.0-SNAPSHOT.jar jsoniter-se
 * ##################Start JsonIterator Serialize
 * ##################JsonIterator :1361
 * ➜  json-serde git:(master) ✗ java -jar target/json-serde-1.0-SNAPSHOT.jar jsoniter-se
 * ##################Start JsonIterator Serialize
 * ##################JsonIterator :1359
 * <p>
 * <p>
 * ➜  json-serde git:(master) ✗ java -jar target/json-serde-1.0-SNAPSHOT.jar jackson-de
 * ##################Start Jackson Deserialize
 * ##################Jackson :1899
 * ➜  json-serde git:(master) ✗ java -jar target/json-serde-1.0-SNAPSHOT.jar jackson-de
 * ##################Start Jackson Deserialize
 * ##################Jackson :2210
 * ➜  json-serde git:(master) ✗ java -jar target/json-serde-1.0-SNAPSHOT.jar jackson-de
 * ##################Start Jackson Deserialize
 * ##################Jackson :2161
 * <p>
 * <p>
 * ➜  json-serde git:(master) ✗ java -jar target/json-serde-1.0-SNAPSHOT.jar jsoniter-de
 * ##################Start JsonIterator Deserialize
 * ##################JsonIterator :1701
 * ➜  json-serde git:(master) ✗ java -jar target/json-serde-1.0-SNAPSHOT.jar jsoniter-de
 * ##################Start JsonIterator Deserialize
 * ##################JsonIterator :1626
 * ➜  json-serde git:(master) ✗ java -jar target/json-serde-1.0-SNAPSHOT.jar jsoniter-de
 * ##################Start JsonIterator Deserialize
 * ##################JsonIterator :1666
 * <p>
 * <p>
 * ➜  json-serde git:(master) ✗ java -jar target/json-serde-1.0-SNAPSHOT.jar mustache
 * ##################Start Mustache
 * ##################Mustache :3957
 * ➜  json-serde git:(master) ✗ java -jar target/json-serde-1.0-SNAPSHOT.jar mustache
 * ##################Start Mustache
 * ##################Mustache :3942
 * ➜  json-serde git:(master) ✗ java -jar target/json-serde-1.0-SNAPSHOT.jar mustache
 * ##################Start Mustache
 * ##################Mustache :3988
 */
@SpringBootApplication
public class JsonIterTest implements CommandLineRunner {

    public static final int ITER_COUNT = 10000;

    public static void main(String... args) {
        SpringApplication.run(JsonIterTest.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        String cmd = args[0];
        if ("jsoniter-de".equals(cmd)) {
            String jsonStr = getJsonStr();
            System.out.println("##################Start JsonIterator Deserialize");
            long time = System.currentTimeMillis();
            for (int i = 0; i < ITER_COUNT; i++) {
                Customer customer = JsonIterator.deserialize(jsonStr, Customer.class);
                if (!"Customer0".equals(customer.getName())) {
                    throw new Error("not equal");
                }
            }

            System.out.println("##################JsonIterator :" + (System.currentTimeMillis() - time));
        } else if ("jackson-de".equals(cmd)) {
            String jsonStr = getJsonStr();
            ObjectMapper mapper = new ObjectMapper();
            System.out.println("##################Start Jackson Deserialize");
            long time = System.currentTimeMillis();
            for (int i = 0; i < ITER_COUNT; i++) {
                Customer customer = mapper.readValue(jsonStr, Customer.class);
                if (!"Customer0".equals(customer.getName())) {
                    throw new Error("not equal");
                }
            }
            System.out.println("##################Jackson :" + (System.currentTimeMillis() - time));
        } else if ("jsoniter-se".equals(cmd)) {
            Customer customer = getCustomer();
            System.out.println("##################Start JsonIterator Serialize");
            long time = System.currentTimeMillis();
            for (int i = 0; i < ITER_COUNT; i++) {
                JsonStream.serialize(customer);
            }
            System.out.println("##################JsonIterator :" + (System.currentTimeMillis() - time));
        } else if ("jackson-se".equals(cmd)) {
            Customer customer = getCustomer();
            ObjectMapper mapper = new ObjectMapper();
            System.out.println("##################Start Jackson Serialize");
            long time = System.currentTimeMillis();
            for (int i = 0; i < ITER_COUNT; i++) {
                mapper.writeValueAsString(customer);
            }
            System.out.println("##################Jackson :" + (System.currentTimeMillis() - time));
        } else if ("mustache".equals(cmd)) {
            Customer customer = getCustomer();
            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile("customer.mustache");
            System.out.println("##################Start Mustache");
            long time = System.currentTimeMillis();
            for (int i = 0; i < ITER_COUNT; i++) {
                mustache.execute(new PrintWriter(new ByteArrayOutputStream()), customer).flush();
            }
            System.out.println("##################Mustache :" + (System.currentTimeMillis() - time));
        }

    }

    private Customer getCustomer() {
        List<Company> companies = IntStream.range(0, 1000)
                .mapToObj(i -> new Company("Company" + i))
                .collect(Collectors.toList());

        return new Customer("CustomerId", "Customer", companies);
    }

    private String getJsonStr() throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader("./customer.txt"))) {
            String buf;
            while ((buf = br.readLine()) != null) {
                sb.append(buf);
            }
        }
        return sb.toString();
    }
}
