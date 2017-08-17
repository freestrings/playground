package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * docker run --name mongo-test -p 0.0.0.0:27017:27017 -p 0.0.0.0:28017:28017 -d mongo
 */
@SpringBootApplication
public class Hello implements CommandLineRunner {

    @Autowired
    private CustomerRepository repository;

    public static void main(String... args) {
        SpringApplication.run(Hello.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        repository.deleteAll();

        Company company1 = new Company("Company1");
        Company company2 = new Company("Company2");

        Customer customer = new Customer();
        customer.setName("Customer1");
        customer.setCompanies(Arrays.asList(new Company[]{company1, company2}));

        repository.save(customer);

        Customer customer1 = repository.findAll().get(0);
        String id = customer1.getId();

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(() -> {
            Assert.isTrue(repository.findOne(id).getCompanies().get(0).getCompanyName().equals("Company1"), "Company1");

            for (int i = 0; i < 1000; i++) {
                repository.updateCompany(id, 0, "Testa" + i);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Customer one = repository.findOne(id);
            List<Company> companies = one.getCompanies();
            Assert.isTrue(companies.get(0).getCompanyName().equals("Testa999"), "Testa");
        });

        executorService.execute(() -> {
            Assert.isTrue(repository.findOne(id).getCompanies().get(1).getCompanyName().equals("Company2"), "Company2");

            for (int i = 0; i < 1000; i++) {
                repository.updateCompany(id, 1, "Testb" + i);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Customer one = repository.findOne(id);
            List<Company> companies = one.getCompanies();
            Assert.isTrue(companies.get(1).getCompanyName().equals("Testb999"), "Testb");
        });

        executorService.shutdown();
    }
}
