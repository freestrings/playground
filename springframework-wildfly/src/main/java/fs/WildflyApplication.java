package fs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class WildflyApplication extends SpringBootServletInitializer {

    public static void main(String... args) {
        SpringApplication.run(WildflyApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(WildflyApplication.class);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @RequestMapping("/")
    public String ping() {
        return jdbcTemplate.query("select 1", (rs, rowNum) -> "ok").stream().findFirst().get();
    }

}
