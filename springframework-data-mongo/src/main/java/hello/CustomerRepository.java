package hello;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerRepository extends CustomerOp, MongoRepository<Customer, String> {

    Customer findByName(String name);
}
