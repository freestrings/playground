package hello;

import org.springframework.data.elasticsearch.core.query.IndexQuery;

import java.util.List;

public interface CustomerOp {

    String updateCompany(String name, String message);

    void updateCompanies(List<String> names, List<String> messages);

    void saveCustomers(List<IndexQuery> queries);

}
