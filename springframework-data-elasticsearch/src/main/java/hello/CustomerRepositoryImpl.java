package hello;

import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;

public class CustomerRepositoryImpl implements CustomerOp {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public int updateCompany(String name, int index, String companyName) {
//        elasticsearchTemplate.update(new UpdateQuery().);
        return 1;
    }

    @Override

    public void findUsingName(String name) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.queryStringQuery(name))
                .build();
//        elasticsearchTemplate.in
    }
}
