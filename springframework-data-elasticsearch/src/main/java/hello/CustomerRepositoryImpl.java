package hello;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQueryBuilder;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CustomerRepositoryImpl implements CustomerOp {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    Client client;

    @Override
    public String updateCompany(String name, String message) {
        UpdateRequest updateRequest = new UpdateRequest();
        HashMap<String, Object> params = new HashMap<>();
        params.put("message", message);
        updateRequest.script(new Script(
                ScriptType.INLINE,
                Script.DEFAULT_SCRIPT_LANG,
                "ctx._source.message=params.message",
                Collections.emptyMap(),
                params));
        UpdateQuery query = new UpdateQueryBuilder()
                .withId(name)
                .withClass(Customer.class)
                .withUpdateRequest(updateRequest).build();
        UpdateResponse update = elasticsearchTemplate.update(query);
        String id = update.getId();
        Assert.isTrue(name.equals(id), "Not same update");
        return id;
    }

    @Override
    public void updateCompanies(List<String> names, List<String> messages) {
//        List<UpdateQuery> queries = new ArrayList<>();
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            String message = messages.get(i);
            UpdateRequest updateRequest = new UpdateRequest();
            updateRequest.index("customer_name");
            updateRequest.type("customer");
            updateRequest.id(name);

            HashMap<String, Object> params = new HashMap<>();
            params.put("companyName", message);
            updateRequest.script(new Script(
                    ScriptType.INLINE,
                    Script.DEFAULT_SCRIPT_LANG,
                    "ctx._source.companies[0].companyName=params.companyName",
                    Collections.emptyMap(),
                    params));


            bulkRequest.add(updateRequest);
//            UpdateQuery query = new UpdateQueryBuilder()
//                    .withId(name)
//                    .withClass(Customer.class)
//                    .withUpdateRequest(updateRequest).build();
//            queries.add(query);
        }
//        try {
//            elasticsearchTemplate.bulkUpdate(queries);
//        } catch (ElasticsearchException e) {
//            Map<String, String> failedDocuments = e.getFailedDocuments();
//            failedDocuments.forEach((s, s2) -> System.out.println("Fail " + s + ": " + s2));
//        }
        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            System.out.println(bulkResponse.buildFailureMessage());
        }
    }

    @Override
    public void saveCustomers(List<IndexQuery> queries) {
        elasticsearchTemplate.bulkIndex(queries);
    }

}
