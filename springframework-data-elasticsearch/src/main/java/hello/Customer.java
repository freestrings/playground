package hello;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Document(indexName = "name", type = "customer")
public class Customer {

    @Id
    public String name;

    @Field(type = FieldType.Nested)
    public List<Company> companies;

    public Customer() {
    }

    public Customer(String name, List<Company> companies) {
        this.name = name;
        this.companies = companies;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Company> getCompanies() {
        return companies;
    }

    public void setCompanies(List<Company> companies) {
        this.companies = companies;
    }

    @Override
    public String toString() {
        return "Customer{" +
                ", name='" + name + '\'' +
                ", companies=" + companies +
                '}';
    }
}
