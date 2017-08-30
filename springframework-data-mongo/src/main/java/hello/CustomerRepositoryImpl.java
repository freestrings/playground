package hello;

import com.mongodb.BulkWriteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.Pair;
import org.springframework.util.Assert;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CustomerRepositoryImpl implements CustomerOp {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public long updateCompany(String name, int index, String companyName) {
        UpdateResult ret = mongoTemplate.updateFirst(
                Query.query(Criteria.where("name").is(name)),
                Update.update("companies." + index + ".companyName", companyName),
                Customer.class
        );
        return ret.getMatchedCount();
    }

    @Override
    public void findUsingName(String name) {
        Customer customer = mongoTemplate.findOne(Query.query(Criteria.where("name").is(name)), Customer.class);
        Assert.isTrue(customer.getName().equals(name), "Not equal");
    }

    @Override
    public int saveCustomers(List<Customer> customers) {
        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Customer.class);
        for (Customer customer : customers) {
            bulkOperations.upsert(
                    Query.query(Criteria.where("name").is(customer.getName())),
                    Update.update("companies", customer.getCompanies()));
        }
        com.mongodb.bulk.BulkWriteResult executed = bulkOperations.execute();
//        System.out.println("inserted: " + executed.getInsertedCount());
//        System.out.println("updated: " + executed.getModifiedCount());
        return executed.getModifiedCount();
    }
}
