package hello;

import java.util.List;

public interface CustomerOp {

    long updateCompany(String name, int index, String companyName);

    Customer findUsingName(String name);

    int saveCustomers(List<Customer> customers);

}
