package hello;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CustomerRepository {

    private Gson gson = new Gson();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void deleteAll() {
        jdbcTemplate.update("delete from customers");
    }

    public void save(Customer customer) {
        jdbcTemplate.update(String.format("insert into customers values ('%s', '%s')", customer.getName(), gson.toJson(customer)));
    }

    public void saveAll(List<Customer> customers) {
        List<Object[]> params = customers.stream().map(customer -> new Object[]{customer.getName(), gson.toJson(customer)}).collect(Collectors.toList());
        jdbcTemplate.batchUpdate("insert into customers values (?, ?)", params);
    }

    public void updateAll(List<Customer> customers) {
        List<String> params = customers
                .stream()
                .map(customer -> String.format(
                        "update customers set data = JSON_SET(data, '$.companies[1].companyName', '%s') where name = '%s'",
                        customer.getMessage(), customer.getName()))
                .collect(Collectors.toList());
//        jdbcTemplate.batchUpdate("update customers set data = JSON_SET(data, '$.companies[1].companyName', ?) where name = ?", params);
        jdbcTemplate.batchUpdate(params.toArray(new String[0]));
    }

    public void update(Customer customer) {
        jdbcTemplate.update(String.format(
                "update customers set data = JSON_SET(data, '$.companies[1].companyName', '%s') where name = '%s'",
                customer.getMessage(), customer.getName()));
    }

    public void upsertAll(List<Customer> customers) {
        List<Customer> inserts = new ArrayList<>();
        List<Customer> updates = new ArrayList<>();
        customers.forEach(customer -> {
            if (findOne(customer.getName()) == null) {
                inserts.add(customer);
            } else {
                updates.add(customer);
            }
        });

        saveAll(inserts);
        updateAll(updates);
    }

    public Customer findOne(String name) {
        Customer customer = jdbcTemplate.queryForObject(
                "select name, data from customers where name = ?",
                new Object[]{name},
                new RowMapper<Customer>() {
                    @Nullable
                    @Override
                    public Customer mapRow(ResultSet resultSet, int i) throws SQLException {
                        String data = resultSet.getString("data");
                        if (data != null) {
                            return gson.fromJson(data, Customer.class);
                        }
                        return null;
                    }
                });

        return customer;
    }
}
