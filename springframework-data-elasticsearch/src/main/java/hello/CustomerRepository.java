package hello;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CustomerRepository extends CustomerOp, ElasticsearchRepository<Customer, String> {

    Customer findByName(String name);
}
