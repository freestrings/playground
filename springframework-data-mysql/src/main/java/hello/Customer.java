package hello;

import java.util.List;

public class Customer {

    public String name;

    public String message;

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

    @Override
    public String toString() {
        return "Customer{" +
                ", name='" + name + '\'' +
                ", companies=" + companies +
                '}';
    }
}
