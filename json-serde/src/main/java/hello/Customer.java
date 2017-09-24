package hello;

import java.util.List;

public class Customer {

    public String id;

    public String name;

    public List<Company> companies;

    public Customer() {
    }

    public Customer(String id, String name, List<Company> companies) {
        this.id = id;
        this.name = name;
        this.companies = companies;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", companies=" + companies +
                '}';
    }
}
