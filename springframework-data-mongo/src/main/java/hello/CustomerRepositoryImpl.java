package hello;

import com.mongodb.WriteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.Assert;

public class CustomerRepositoryImpl implements CustomerOp {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public int updateCompany(String name, int index, String companyName) {
        WriteResult ret = mongoTemplate.updateFirst(
                Query.query(Criteria.where("name").is(name)),
                Update.update("companies." + index + ".companyName", companyName),
                Customer.class
        );
        return ret.getN();
    }

    @Override
    public void findUsingName(String name) {
        Customer customer = mongoTemplate.findOne(Query.query(Criteria.where("name").is(name)), Customer.class);
        Assert.isTrue(customer.getName().equals(name), "Not equal");
    }
}
