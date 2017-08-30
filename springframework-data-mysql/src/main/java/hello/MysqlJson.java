package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * sudo service mysql start
 *
 * create database testa;
 * use testa;
 * create table customers (
 * name varchar(20) not null,
 * data json not null,
 * PRIMARY KEY (name)
 * );
 */
@SpringBootApplication
public class MysqlJson implements CommandLineRunner {

    @Autowired
    CustomerRepository repository;

    public static void main(String... args) {
        SpringApplication.run(MysqlJson.class);
    }

    @Override
    public void run(String... args) throws Exception {

//        List<Company> companies = IntStream.range(0, 1000)
//                .mapToObj(i -> new Company("Company" + i))
//                .collect(Collectors.toList());
//        Customer customer = new Customer("Customer", "message", companies);
//        repository.save(customer);

        repository.updateCompany("Customer", "ABCD");
    }
}
