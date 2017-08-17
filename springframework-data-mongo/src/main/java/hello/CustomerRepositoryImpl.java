package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class CustomerRepositoryImpl implements CustomerOp {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public void updateCompany(String id, int index, String name) {
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("id").is(id)),
                Update.update("companies." + index + ".companyName", name),
                Customer.class
        );
    }
}
