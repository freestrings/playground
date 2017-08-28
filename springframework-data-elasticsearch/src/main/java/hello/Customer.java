package hello;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;

import java.util.List;

@Document(indexName = "customer_name")
public class Customer {

    @Id
    public String name;

    public String message;

    @Field(type = FieldType.Nested)
    public List<Company> companies;

    public Customer() {
    }

    public Customer(String name, String message, List<Company> companies) {
        this.name = name;
        this.message = message;
        this.companies = companies;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Company> getCompanies() {
        return companies;
    }

    public void setCompanies(List<Company> companies) {
        this.companies = companies;
    }

    public IndexQuery toIndexQuery() {
        return new IndexQueryBuilder()
                .withId(this.name)
                .withObject(this).build();
    }

    @Override
    public String toString() {
        return "Customer{" +
                ", name='" + name + '\'' +
                ", companies=" + companies +
                '}';
    }
}
