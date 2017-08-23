package hello;

public interface CustomerOp {

    int updateCompany(String name, int index, String companyName);

    Customer findUsingName(String name);

}
