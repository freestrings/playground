package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @ docker-compose up -> NoNodeAvailableException[None of the configured nodes are available: [{#transport#-1}{c-Zouq4-QMWvuHcDwswn9A}{127.0.0.1}{127.0.0.1:9300}]] 에러남
 * @ https://www.elastic.co/guide/en/elasticsearch/reference/5.4/zip-targz.html install
 *  - elasticsearch-5.4.3/config/elasticsearch.yml => cluster.name: customers으로 수정
 *  - export=ES_JAVA_OPTS="-Xms512m -Xmx512m";./bin/elasticsearch
 */
@SpringBootApplication
public class ESTest implements CommandLineRunner {

    @Autowired
    private CustomerRepository repository;

    public static void main(String... args) {
        SpringApplication.run(ESTest.class, args);
    }

    class _Insert implements Runnable {

        private final int id;
        private final int work;
        private final long initialTime;
        private long time;

        _Insert(int id, int work) {
            this.id = id;
            this.work = work;
            this.initialTime = this.time = System.currentTimeMillis();
        }

        @Override
        public void run() {
            int index = this.id * this.work;

            List<Company> companies = IntStream.range(0, 1000)
                    .mapToObj(i -> new Company("Company" + i))
                    .collect(Collectors.toList());

            IntStream.range(index, index + this.work).forEach(c -> {
                Customer customer = new Customer("Customer" + c, companies);
                repository.save(customer);
                if (c % 1000 == 0 && c > 0) {
                    long _time = System.currentTimeMillis();
                    System.out.println(c + ":" + (_time - this.time));
                    this.time = _time;
                }
            });
            System.out.println("Insert done" + this.id + ": " + (System.currentTimeMillis() - this.initialTime) / 1000.0);
        }
    }

    class _Select implements Runnable {

        private final int id;
        private final int work;
        private final long initialTime;
        private long time;

        _Select(int id, int work) {
            this.id = id;
            this.work = work;
            this.initialTime = this.time = System.currentTimeMillis();
        }

        @Override
        public void run() {
            int index = this.id * this.work;
            IntStream.range(index, index + this.work).forEach(c -> {
                Customer customer = repository.findByName("Customer" + c);
                Assert.isTrue(customer.getName().equals("Customer" + c), "Not match " + c);
                if (c % 1000 == 0 && c > 0) {
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
        private long time;

        _Update(int id, int work) {
            this.id = id;
            this.work = work;
            this.initialTime = this.time = System.currentTimeMillis();
        }

        @Override
        public void run() {
            int index = this.id * this.work;
            IntStream.range(index, index + this.work).forEach(c -> {
                int ret = repository.updateCompany("Customer" + c, 20, "Random");
                Assert.isTrue(ret == 1, "Fail to update");
                if (c % 1000 == 0 && c > 0) {
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
        if ("insert".equals(command)) {
            System.out.println("Start Insert");
            int worker = 4;
            int total = 10000;
            ExecutorService executorService = Executors.newFixedThreadPool(worker);
            for (int t = 0; t < worker; t++) {
                executorService.execute(new _Insert(t, total / worker));
            }
            executorService.shutdown();

        } else if ("insertOne".equals(command)) {
            System.out.println("Start");
            new Thread(new _Insert(0, 1)).start();
        } else if ("select".equals(command)) {
            System.out.println("Start Select");
            int worker = 4;
            int total = 10000;
            ExecutorService executorService = Executors.newFixedThreadPool(worker);
            for (int t = 0; t < worker; t++) {
                executorService.execute(new _Select(t, total / worker));
            }
            executorService.shutdown();
        } else if ("selectOne".equals(command)) {
            System.out.println("Start Select One");
//            repository.findUsingName("Customer0");
            Customer customer0 = repository.findByName("Customer0");
            Assert.isTrue(customer0.getName().equals("Customer0"), "Not matched");
        } else if ("select1000".equals(command)) {
            System.out.println("Start Select 1000");
            long t = System.currentTimeMillis();
            IntStream.range(0, 1000).forEach(i -> repository.findUsingName("Customer" + i));
            System.out.println(System.currentTimeMillis() - t);
        } else if ("update".equals(command)) {
            System.out.println("Start Update");
            int worker = 4;
            int total = 100000;
            ExecutorService executorService = Executors.newFixedThreadPool(worker);
            for (int t = 0; t < worker; t++) {
                executorService.execute(new _Update(t, total / worker));
            }
            executorService.shutdown();
        } else if ("delete".equals(command)) {
            repository.deleteAll();
        }

    }
}
