package hello;

import java.util.List;

public interface CustomerOp {

    int updateCompany(String name, int index, String companyName);

    void findUsingName(String name);

    int saveCustomers(List<Customer> customers);

}
