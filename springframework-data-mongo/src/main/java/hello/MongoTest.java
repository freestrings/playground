package hello;

import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * docker run --name mongo-test -p 0.0.0.0:27017:27017 -p 0.0.0.0:28017:28017 -d mongo
 * <p>
 * ➜  springframework-data-mongo git:(master) ✗ java -jar target/springframework-data-mongo-1.0-SNAPSHOT.jar insert 10000 4 50 -10000:4:50-
 * <p>
 * Start Insert
 * Insert done0: 11.355
 * Insert done3: 11.388
 * Insert done2: 11.457
 * Insert done1: 11.627
 * <p>
 * springframework-data-mongo git:(master) ✗ java -jar target/springframework-data-mongo-1.0-SNAPSHOT.jar insert 10000 4 100 -10000:4:100-
 * <p>
 * Insert done0: 11.392
 * Insert done3: 11.595
 * Insert done1: 11.655
 * Insert done2: 11.691
 * <p>
 * ➜  springframework-data-mongo git:(master) ✗ java -jar target/springframework-data-mongo-1.0-SNAPSHOT.jar insert 10000 4 200 -10000:4:200-
 * <p>
 * Insert done0: 13.337
 * Insert done3: 13.852
 * Insert done1: 14.052
 * Insert done2: 14.176
 * <p>
 * ➜  springframework-data-mongo git:(master) ✗ java -jar target/springframework-data-mongo-1.0-SNAPSHOT.jar update 10000 4 100 -10000:4:100:update-
 * <p>
 * Update done2: 4.203
 * Update done0: 4.239
 * Update done1: 4.346
 * Update done3: 4.361
 * <p>
 * ➜  springframework-data-mongo git:(master) ✗ java -jar target/springframework-data-mongo-1.0-SNAPSHOT.jar select 10000 4 1000 0
 * Select done2: 7.374
 * Select done0: 7.439
 * Select done3: 7.631
 * Select done1: 7.648
 * <p>
 * ➜  springframework-data-mongo git:(master) ✗ java -jar target/springframework-data-mongo-1.0-SNAPSHOT.jar select 10000 2 1000 0
 * <p>
 * Start Select
 * Select done1: 8.769
 * Select done0: 8.78
 * ➜  springframework-data-mongo git:(master) ✗ java -jar target/springframework-data-mongo-1.0-SNAPSHOT.jar select 10000 4 1000 0
 * <p>
 * Start Select
 * Select done2: 6.671
 * Select done3: 6.738
 * Select done0: 6.759
 * Select done1: 6.777
 * <p>
 * -----------------------------------------------------------
 * using mongo api directly
 * <p>
 * insert 10000 4 1000 -10000:4:100-
 * Insert done3: 10.553
 * Insert done1: 10.725
 * Insert done0: 10.783
 * Insert done2: 10.913
 * <p>
 * select 10000 4 1000 0
 * Select done2: 3.162
 * Select done1: 3.263
 * Select done3: 3.278
 * Select done0: 3.333
 * <p>
 * select 10000 4 1000 0
 * Select done3: 2.787
 * Select done1: 2.936
 * Select done2: 2.943
 * Select done0: 2.957
 * <p>
 * update 10000 4 1000 -10000:4:1000:update-
 * Update done0: 2.737
 * Update done3: 2.82
 * Update done2: 2.84
 * Update done1: 2.883
 */
@SpringBootApplication
public class MongoTest implements CommandLineRunner {

    @Autowired
    private CustomerRepository repository;

    @Bean
    public MongoClient mongoClient() {
        return new MongoClient("localhost", 27017);
    }

    @Autowired
    MongoClient mongoClient;

//    @PostConstruct
//    public void init() {
//        IndexOptions indexOptions = new IndexOptions().unique(true);
//        MongoDatabase database = mongoClient.getDatabase("testa");
//        MongoCollection<Document> collection = database.getCollection("customers");
//        ListIndexesIterable<Document> documents = collection.listIndexes();
//        for (Document document : documents) {
//            System.out.println(document);
//            Object name = document.get("name");
//        }
//    }

    public static void main(String... args) {
        SpringApplication.run(MongoTest.class, args);
    }

    class _Insert implements Runnable {

        private final int id;
        private final int work;
        private final long initialTime;
        private final int commitCount;
        private final String token;
        private long time;

        _Insert(int id, int work, int commitCount, String token) {
            this.id = id;
            this.work = work;
            this.commitCount = commitCount;
            this.token = token;
            this.initialTime = this.time = System.currentTimeMillis();
        }

        @Override
        public void run() {
            int index = this.id * this.work;

            List<Company> companies = IntStream.range(0, 1000)
                    .mapToObj(i -> new Company("Company" + token + i))
                    .collect(Collectors.toList());

            List<Customer> customers = new ArrayList<>();
            IntStream.range(index, index + this.work).forEach(c -> {
                Customer customer = new Customer();
                customer.setName("Customer" + c);
                customer.setCompanies(companies);
                customers.add(customer);
                if (c % commitCount == 0 && c > 0) {
                    int result = repository.saveCustomers(customers);
                    Assert.isTrue(customers.size() == result, "Fail to insert");
                    customers.clear();
                    long _time = System.currentTimeMillis();
                    System.out.println(c + ":" + (_time - this.time));
                    this.time = _time;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                }
            });

            if (customers.size() > 0) {
                int result = repository.saveCustomers(customers);
                Assert.isTrue(customers.size() == result, "Fail to insert");
            }
            System.out.println("Insert done" + this.id + ": " + (System.currentTimeMillis() - this.initialTime) / 1000.0);
        }
    }

    class _Select implements Runnable {

        private final int id;
        private final int work;
        private final long initialTime;
        private final int commitCount;
        private long time;

        _Select(int id, int work, int commitCount) {
            this.id = id;
            this.work = work;
            this.commitCount = commitCount;
            this.initialTime = this.time = System.currentTimeMillis();
        }

        @Override
        public void run() {
            int index = this.id * this.work;
            IntStream.range(index, index + this.work).forEach(c -> {
//                Customer customer = repository.findByName("Customer" + c);
                Customer customer = repository.findUsingName("Customer" + c);
                Assert.isTrue(customer.getName().equals("Customer" + c), "Not match " + c);
                if (c % commitCount == 0 && c > 0) {
                    long _time = System.currentTimeMillis();
                    System.out.println(c + ":" + (_time - this.time));
                    this.time = _time;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                }
            });
            System.out.println("Select done" + this.id + ": " + (System.currentTimeMillis() - this.initialTime) / 1000.0);
        }
    }

    class _Update implements Runnable {

        private final int id;
        private final int work;
        private final long initialTime;
        private final int commitCount;
        private final String token;
        private long time;

        _Update(int id, int work, int commitCount, String token) {
            this.id = id;
            this.work = work;
            this.commitCount = commitCount;
            this.token = token;
            this.initialTime = this.time = System.currentTimeMillis();
        }

        @Override
        public void run() {
            int index = this.id * this.work;
            IntStream.range(index, index + this.work).forEach(c -> {
                long ret = repository.updateCompany("Customer" + c, 20, "Random" + token);
                Assert.isTrue(ret == 1, "Fail to update");
                if (c % commitCount == 0 && c > 0) {
                    long _time = System.currentTimeMillis();
                    System.out.println(c + ":" + (_time - this.time));
                    this.time = _time;
                }
            });
            System.out.println("Update done" + this.id + ": " + (System.currentTimeMillis() - this.initialTime) / 1000.0);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            return;
        }
        String command = args[0];
        int total = Integer.parseInt(args[1]);
        int worker = Integer.parseInt(args[2]);
        int commitCount = Integer.parseInt(args[3]);
        String token = args[4];
        if ("insert".equals(command)) {
            System.out.println("Start Insert");
            ExecutorService executorService = Executors.newFixedThreadPool(worker);
            for (int t = 0; t < worker; t++) {
                executorService.execute(new _Insert(t, total / worker, commitCount, token));
            }
            executorService.shutdown();

        } else if ("insertOne".equals(command)) {
            System.out.println("Start");
            new Thread(new _Insert(0, 1, commitCount, token)).start();
        } else if ("select".equals(command)) {
            System.out.println("Start Select");
            ExecutorService executorService = Executors.newFixedThreadPool(worker);
            for (int t = 0; t < worker; t++) {
                executorService.execute(new _Select(t, total / worker, commitCount));
            }
            executorService.shutdown();
        } else if ("selectOne".equals(command)) {
            System.out.println("Start Select One");
            repository.findUsingName("Customer1");
        } else if ("select1000".equals(command)) {
            System.out.println("Start Select 1000");
            long t = System.currentTimeMillis();
            IntStream.range(0, 1000).forEach(i -> repository.findUsingName("Customer" + i));
            System.out.println(System.currentTimeMillis() - t);
        } else if ("update".equals(command)) {
            System.out.println("Start Update");
            ExecutorService executorService = Executors.newFixedThreadPool(worker);
            for (int t = 0; t < worker; t++) {
                executorService.execute(new _Update(t, total / worker, commitCount, token));
            }
            executorService.shutdown();
        } else if ("delete".equals(command)) {
            System.out.println("Start Delete");
            repository.deleteAll();
        }

    }
}
