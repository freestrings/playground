package hello;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CustomerRepositoryImpl implements CustomerOp {

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    MongoClient mongoClient;

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
    public Customer findUsingName(String name) {
//        Customer customer = mongoTemplate.findOne(Query.query(Criteria.where("name").is(name)), Customer.class);
//        Assert.isTrue(customer.getName().equals(name), "Not equal");
        MongoDatabase db = mongoClient.getDatabase("testa");
        MongoCollection<Document> customers = db.getCollection("customers");
        BasicDBObject name1 = new BasicDBObject().append("name", name);
        FindIterable<Document> documents = customers.find(name1);
        Document first = documents.first();
        Customer customer = new Customer();
        customer.setId(first.getObjectId("_id").toString());
        customer.setName(first.getString("name"));
        customer.setCompanies((List<Company>) first.get("companies", List.class).stream()
                .map((Function<Document, Company>) doc -> new Company(doc.getString("companyName")))
                .collect(Collectors.toList()));
        return customer;
    }

    @Override
    public int saveCustomers(List<Customer> customers) {
//        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Customer.class);
//        for (Customer customer : customers) {
//            bulkOperations.upsert(
//                    Query.query(Criteria.where("name").is(customer.getName())),
//                    Update.update("companies", customer.getCompanies()));
//        }
//        com.mongodb.bulk.BulkWriteResult executed = bulkOperations.execute();
////        System.out.println("inserted: " + executed.getInsertedCount());
////        System.out.println("updated: " + executed.getModifiedCount());
//        return executed.getModifiedCount();

        MongoDatabase db = mongoClient.getDatabase("testa");
        MongoCollection<Document> customerCollection = db.getCollection("customers");
        List<InsertOneModel<Document>> cusutomerCollections = customers.stream().map(customer -> {
            Document doc = new Document();
            doc.put("name", customer.getName());
            List<BasicDBObject> companayName = customer.getCompanies()
                    .stream()
                    .map(company -> new BasicDBObject().append("companayName", company.getCompanyName()))
                    .collect(Collectors.toList());

            doc.put("companies", companayName);
            return new InsertOneModel<>(doc);
        }).collect(Collectors.toList());
        BulkWriteResult bulkWriteResult = customerCollection.bulkWrite(cusutomerCollections);
        return bulkWriteResult.getInsertedCount();
    }
}
