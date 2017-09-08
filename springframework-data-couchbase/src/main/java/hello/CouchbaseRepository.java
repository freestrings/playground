package hello;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouchbaseRepository extends CrudRepository<Customer, String> {
}
