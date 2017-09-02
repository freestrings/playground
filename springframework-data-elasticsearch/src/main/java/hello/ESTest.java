package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @ 65530 -> sysctl -w vm.max_map_count=262144
 * @ docker-compose up -> NoNodeAvailableException[None of the configured nodes are available: [{#transport#-1}{c-Zouq4-QMWvuHcDwswn9A}{127.0.0.1}{127.0.0.1:9300}]] 에러남
 * @ https://www.elastic.co/guide/en/elasticsearch/reference/5.4/zip-targz.html install
 * - elasticsearch-5.4.3/config/elasticsearch.yml => cluster.name: customers으로 수정
 * - export=ES_JAVA_OPTS="-Xms512m -Xmx512m";./bin/elasticsearch
 * <p>
 * 세팅조회
 * http://localhost:9200/_settings
 * <p>
 * 수정
 * curl -XPUT 'http://localhost:9200/customer_name/_settings' -d '{
 * "index.refresh_interval" : "30s"
 * }'
 * <p>
 * curl -XPUT 'http://localhost:9200/_all/_settings' -d '{
 * "index.refresh_interval" : "1s"
 * }'
 * <p>
 * ➜  springframework-data-elasticsearch git:(master) ✗ java -jar target/springframework-data-elasticsearch-1.0-SNAPSHOT.jar insert 10000 4 1000 -10000:4:1000:1s-
 * <p>
 * Start Insert
 * Insert done1: 19.25
 * Insert done3: 20.942
 * Insert done0: 21.778
 * Insert done2: 22.964
 * ➜  springframework-data-elasticsearch git:(master) ✗ curl -XPUT 'http://localhost:9200/customer_name/_settings' -d '{
 * "index.refresh_interval" : "30s"
 * }'
 * {"acknowledged":true}%                                                                                                                                                                                        ➜  springframework-data-elasticsearch git:(master) ✗ java -jar target/springframework-data-elasticsearch-1.0-SNAPSHOT.jar insert 10000 4 1000 -10000:4:1000:30s-
 * <p>
 * ➜  springframework-data-elasticsearch git:(master) ✗ java -jar target/springframework-data-elasticsearch-1.0-SNAPSHOT.jar insert 10000 4 1000 -10000:4:1000:30s-
 * <p>
 * Start Insert
 * Insert done1: 23.102
 * Insert done3: 24.699
 * Insert done2: 25.796
 * Insert done0: 26.813
 * <p>
 * ➜  springframework-data-elasticsearch git:(master) ✗ java -jar target/springframework-data-elasticsearch-1.0-SNAPSHOT.jar select 10000 4 1000 \2
 * <p>
 * Start Select
 * Select done3: 3.731
 * Select done1: 3.79
 * Select done2: 3.854
 * Select done0: 3.885
 * ➜  springframework-data-elasticsearch git:(master) ✗ java -jar target/springframework-data-elasticsearch-1.0-SNAPSHOT.jar select 10000 4 1000 0
 * <p>
 * Start Select
 * Select done3: 2.891
 * Select done2: 2.919
 * Select done0: 2.923
 * Select done1: 2.95
 * ➜  springframework-data-elasticsearch git:(master) ✗ java -jar target/springframework-data-elasticsearch-1.0-SNAPSHOT.jar select 10000 2 1000 0
 * <p>
 * Start Select
 * Select done0: 3.501
 * Select done1: 3.542
 * <p>
 * <p>
 * curl -XPUT 'http://localhost:9200/_all/_settings' -d '{
 * "index.refresh_interval" : "1s"
 * }
 * ➜  springframework-data-elasticsearch git:(master) ✗ java -jar target/springframework-data-elasticsearch-1.0-SNAPSHOT.jar update 10000 4 1000 \!
 * Update done3: 19.137
 * Update done1: 22.51
 * Update done2: 24.153
 * Update done0: 25.496
 * <p>
 * curl -XPUT 'http://localhost:9200/_all/_settings' -d '{
 * "index.refresh_interval" : "30s"
 * }
 * ➜ springframework-data-elasticsearch git:(master) ✗ java -jar target/springframework-data-elasticsearch-1.0-SNAPSHOT.jar update 10000 4 1000 \#
 * <p>
 * Update done0: 20.416
 * Update done3: 23.73
 * Update done1: 26.015
 * Update done2: 26.554
 * <p>
 * <p>
 * >>> 5.5.2
 * ➜ insert 10000 4 1000 -
 * <p>
 * Insert done3: 21.861
 * Insert done1: 23.565
 * Insert done2: 24.149
 * Insert done0: 25.053
 * <p>
 * curl -XPUT 'http://localhost:9200/_all/_settings' -d '{
 * "index.refresh_interval" : "30s"
 * }'
 * <p>
 * ➜ insert 10000 4 1000 \!
 * <p>
 * Insert done3: 17.196
 * Insert done1: 19.805
 * Insert done2: 20.762
 * Insert done0: 21.683
 * <p>
 * ➜ update 10000 4 1000 \#
 * <p>
 * Update done1: 19.423
 * Update done0: 19.986
 * Update done3: 22.848
 * Update done2: 23.084
 * <p>
 * ➜ select 10000 4 1000 \#
 * <p>
 * Select done1: 3.36
 * Select done3: 3.485
 * Select done2: 3.499
 * Select done0: 3.51
 */
@SpringBootApplication
public class ESTest implements CommandLineRunner {

    @Autowired
    private CustomerRepository repository;

    public static void main(String... args) {
        SpringApplication.run(ESTest.class, args);
    }

    class _Insert implements Runnable {

        private final int start;
        private final int end;
        private final long initialTime;
        private final int countCount;
        private final String token;
        private long time;

        _Insert(int start, int end, int countCount, String token) {
            this.start = start;
            this.end = end;
            this.countCount = countCount;
            this.token = token;
            this.initialTime = this.time = System.currentTimeMillis();
        }

        @Override
        public void run() {
            List<Company> companies = IntStream.range(0, 1000)
                    .mapToObj(i -> new Company("Company" + i))
                    .collect(Collectors.toList());

            List<IndexQuery> queries = new ArrayList<>();
            IntStream.range(start, this.end).forEach(c -> {
                Customer customer = new Customer("Customer" + c, "message" + token + c, companies);
                IndexQuery indexQuery = customer.toIndexQuery();
                queries.add(indexQuery);
                if (c % countCount == 0 && c > 0) {
                    repository.saveCustomers(queries);
                    queries.clear();
                    long _time = System.currentTimeMillis();
                    System.out.println(c + ":" + (_time - this.time));
                    this.time = _time;
                }
            });

            if (queries.size() > 0) {
                repository.saveCustomers(queries);
            }
            System.out.println("Insert done" + this.start + ": " + (System.currentTimeMillis() - this.initialTime) / 1000.0);
        }
    }

    class _Select implements Runnable {

        private final int start;
        private final int end;
        private final long initialTime;
        private final int commitCount;
        private long time;

        _Select(int start, int end, int commitCount) {
            this.start = start;
            this.end = end;
            this.commitCount = commitCount;
            this.initialTime = this.time = System.currentTimeMillis();
        }

        @Override
        public void run() {
            IntStream.range(start, this.end).forEach(c -> {
                Optional<Customer> byId = repository.findById("Customer" + c);
                Customer customer = byId.get();
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
            System.out.println("Select done" + this.start + ": " + (System.currentTimeMillis() - this.initialTime) / 1000.0);
        }
    }

    class _Update implements Runnable {

        private final int start;
        private final int end;
        private final long initialTime;
        private final int commitCount;
        private final String token;
        private long time;

        _Update(int start, int end, int commitCount, String token) {
            this.start = start;
            this.end = end;
            this.commitCount = commitCount;
            this.token = token;
            this.initialTime = this.time = System.currentTimeMillis();
        }

        @Override
        public void run() {
            List<String> names = new ArrayList<>();
            List<String> messages = new ArrayList<>();
            IntStream.range(start, end).forEach(c -> {
                names.add("Customer" + c);
                messages.add("message" + token + c);
                if (c % commitCount == 0 && c > 0) {
                    repository.updateCompanies(names, messages);
                    names.clear();
                    messages.clear();
                    long _time = System.currentTimeMillis();
                    System.out.println(c + ":" + (_time - this.time));
                    this.time = _time;
                }
            });

            if (names.size() > 0) {
                repository.updateCompanies(names, messages);
            }
            System.out.println("Update done" + this.start + ": " + (System.currentTimeMillis() - this.initialTime) / 1000.0);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            System.out.println("command <count> <worker> <commitCount> <token>");
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
                executorService.execute(new _Insert(t * total, t * total + total, commitCount, token));
            }
            executorService.shutdown();
        } else if ("insertOne".equals(command)) {
            System.out.println("Start");
            List<Company> companies = IntStream.range(0, 1000)
                    .mapToObj(i -> new Company("Company" + i))
                    .collect(Collectors.toList());
            Customer customer0 = new Customer("Customer0", "message-0", companies);
            repository.save(customer0);
        } else if ("select".equals(command)) {
            System.out.println("Start Select");
            ExecutorService executorService = Executors.newFixedThreadPool(worker);
            for (int t = 0; t < worker; t++) {
                executorService.execute(new _Select(t * total, t * total + total, commitCount));
            }
            executorService.shutdown();
        } else if ("selectOne".equals(command)) {
            System.out.println("Start Select One: " + total);
            Customer customer = repository.findByName("Customer" + total);
            Assert.isTrue(customer.getName().equals("Customer" + total), "Not matched");
        } else if ("update".equals(command)) {
            System.out.println("Start Update");
            ExecutorService executorService = Executors.newFixedThreadPool(worker);
            for (int t = 0; t < worker; t++) {
                executorService.execute(new _Update(t * total, t * total + total, commitCount, token));
            }
            executorService.shutdown();
        } else if ("updateOne".equals(command)) {
            System.out.println("Start Update One");
            String id = "Customer" + total;
            String result = repository.updateCompany(id, "testa");
            Assert.isTrue(result.equals(id), "Fail to check update result");
        } else if ("delete".equals(command)) {
            repository.deleteAll();
        } else if ("update-single".equals(command)) {
            System.out.println("Start Update Single: " + worker * total + "~" + (worker * total + total));
            new _Update(worker * total, worker * total + total, commitCount, token).run();
        }
    }
}
