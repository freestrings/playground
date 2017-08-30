package hello;

import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepositoryImpl implements CustomerOp {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public String updateCompany(String name, String message) {
        UpdateRequest updateRequest = new UpdateRequest();
        try {
            updateRequest.doc(XContentFactory.jsonBuilder()
                    .startObject()
                    .field("message", message)
                    .endObject());
        } catch (IOException e) {
            e.printStackTrace();
        }
        UpdateQuery query = new UpdateQueryBuilder()
                .withId(name)
                .withClass(Customer.class)
                .withUpdateRequest(updateRequest).build();
        UpdateResponse update = elasticsearchTemplate.update(query);
        String id = update.getId();
        Assert.isTrue(name.equals(id), "Not same update");
        return id;

        /**
         * Caused by: CircuitBreakingException[[script] Too many dynamic script compilations within one minute, max: [15/min]; please use on-disk, indexed, or scripts with parameters instead; this limit can be changed by the [script.max_compilations_per_minute] setting]
         */
//        UpdateRequest updateRequest = new UpdateRequest();
//        updateRequest.script(new Script("ctx._source.message=\"" + message+"\""));
//        UpdateQuery query = new UpdateQueryBuilder()
//                .withId(name)
//                .withClass(Customer.class)
//                .withUpdateRequest(updateRequest).build();
//        UpdateResponse update = elasticsearchTemplate.update(query);
//        String id = update.getId();
//        Assert.isTrue(name.equals(id), "Not same update");
//        return id;
    }

    @Override
    public void updateCompanies(List<String> names, List<String> messages) {
        List<UpdateQuery> queries = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            String message = messages.get(i);
            UpdateRequest updateRequest = new UpdateRequest();
            try {
                updateRequest.doc(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("message", message)
                        .endObject());
            } catch (IOException e) {
                e.printStackTrace();
            }
            UpdateQuery query = new UpdateQueryBuilder()
                    .withId(name)
                    .withClass(Customer.class)
                    .withUpdateRequest(updateRequest).build();
            queries.add(query);
        }
        elasticsearchTemplate.bulkUpdate(queries);
    }

    @Override
    public void saveCustomers(List<IndexQuery> queries) {
        elasticsearchTemplate.bulkIndex(queries);
    }

}
