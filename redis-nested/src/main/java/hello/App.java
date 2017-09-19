package hello;

import redis.clients.jedis.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class App {

    public static void main(String... args) {

        JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");

        int total = 10000;
        int amount = 100;
        long time = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(total / amount);
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        for (int i = 0; i < total; i += amount) {
            executorService.execute(new Op(latch, pool.getResource(), i, amount));
        }
        try {
            latch.await();
            executorService.shutdown();
        } catch (InterruptedException e) {
        }
        System.out.println("Total:" + (System.currentTimeMillis() - time));
    }

    static class Op implements Runnable {
        private final CountDownLatch latch;
        private final Jedis jedis;
        private final int start;
        private final int amount;

        public Op(CountDownLatch latch, Jedis jedis, int start, int amount) {
            this.latch = latch;
            this.jedis = jedis;
            this.start = start;
            this.amount = amount;
        }

        @Override
        public void run() {
            try {
                List<Company> companiesOrig = IntStream.range(0, 1000)
                        .mapToObj(i -> new Company("Company" + i))
                        .collect(Collectors.toList());

                List<Customer> customers = IntStream.range(start, start + amount).mapToObj(i -> {
                    Customer customer = new Customer();
                    customer.setName("customer" + i);
                    customer.setId("id" + i);
                    customer.setCompanies(companiesOrig);
                    return customer;
                }).collect(Collectors.toList());

                long time = System.currentTimeMillis();
                Pipeline pipelined = jedis.pipelined();
                customers.forEach(c -> {
                    pipelined.hset(c.getId(), "name", c.getName());
                    List<Company> companies = c.getCompanies();
                    for (int i = 0; i < companies.size(); i++) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("company.").append(i).append(".companyName");
                        pipelined.hset(c.getId(), sb.toString(), companies.get(i).getCompanyName());
                    }
                });
                pipelined.sync();
                System.out.println(start + ":" + (System.currentTimeMillis() - time));
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }

            latch.countDown();
        }
    }

}
