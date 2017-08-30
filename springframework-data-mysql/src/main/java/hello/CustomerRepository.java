package hello;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomerRepository {

    private Gson gson = new Gson();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void save(Customer customer) {
        jdbcTemplate.update(String.format("insert into customers values ('%s', '%s')", customer.getName(), gson.toJson(customer)));
    }

    public void updateCompany(String name, String message) {
        jdbcTemplate.update(String.format(
                "update customers set data = JSON_SET(data, '$.companies[1].companyName', '%s') where name = '%s'",
                message, name));
    }

    public void updateCompanies(List<String> names, List<String> messages) {

    }
}
