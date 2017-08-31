package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * sudo service mysql start
 * <p>
 * create database testa;
 * use testa;
 * create table customers (
 * name varchar(20) not null,
 * data json not null,
 * PRIMARY KEY (name)
 * );
 * <p>
 * <p>
 * ➜  springframework-data-mysql git:(master) ✗ java -jar target/springframework-data-mysql-0.0.1-SNAPSHOT.jar upsert 10000 4 1000 \!
 * Start Upsert
 * Upsert done0: 15.403
 * Upsert done1: 15.43
 * Upsert done2: 15.619
 * Upsert done3: 15.659
 * <p>
 * ➜  springframework-data-mysql git:(master) ✗ java -jar target/springframework-data-mysql-0.0.1-SNAPSHOT.jar select 10000 4 1000 \!
 * Start Select
 * Select done3: 2.74
 * Select done1: 2.914
 * Select done2: 2.917
 * Select done0: 3.029
 * <p>
 * ➜  springframework-data-mysql git:(master) ✗ java -jar target/springframework-data-mysql-0.0.1-SNAPSHOT.jar insert 10000 4 1000 \!
 * Start Insert
 * Insert done3: 16.372
 * Insert done2: 16.398
 * Insert done1: 16.43
 * Insert done0: 16.437
 */
@SpringBootApplication
public class MysqlJson implements CommandLineRunner {

    @Autowired
    CustomerRepository repository;

    public static void main(String... args) {
        SpringApplication.run(MysqlJson.class, args);
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
                    repository.saveAll(customers);
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
                repository.saveAll(customers);
            }
            System.out.println("Insert done" + this.id + ": " + (System.currentTimeMillis() - this.initialTime) / 1000.0);
        }
    }

    class _Upsert implements Runnable {

        private final int id;
        private final int work;
        private final long initialTime;
        private final int commitCount;
        private final String token;
        private long time;

        _Upsert(int id, int work, int commitCount, String token) {
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
                customer.setMessage("Message" + token);
                customer.setCompanies(companies);
                customers.add(customer);
                if (c % commitCount == 0 && c > 0) {
                    repository.upsertAll(customers);
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
                repository.upsertAll(customers);
            }
            System.out.println("Upsert done" + this.id + ": " + (System.currentTimeMillis() - this.initialTime) / 1000.0);
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
                Customer customer = repository.findOne("Customer" + c);
                Assert.isTrue(customer.getName().equals("Customer" + c), "Not match " + c);
                if (c % commitCount == 0 && c > 0) {
                    long _time = System.currentTimeMillis();
                    System.out.println(c + ":" + (_time - this.time));
                    this.time = _time;
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                    }
                }
            });
            System.out.println("Select done" + this.id + ": " + (System.currentTimeMillis() - this.initialTime) / 1000.0);
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
        } else if ("delete".equals(command)) {
            System.out.println("Delete All");
            repository.deleteAll();
        } else if ("select".equals(command)) {
            System.out.println("Start Select");
            ExecutorService executorService = Executors.newFixedThreadPool(worker);
            for (int t = 0; t < worker; t++) {
                executorService.execute(new _Select(t, total / worker, commitCount));
            }
            executorService.shutdown();
        } else if ("upsert".equals(command)) {
            System.out.println("Start Upsert");
            ExecutorService executorService = Executors.newFixedThreadPool(worker);
            for (int t = 0; t < worker; t++) {
                executorService.execute(new _Upsert(t, total / worker, commitCount, token));
            }
            executorService.shutdown();
        }
    }
}
