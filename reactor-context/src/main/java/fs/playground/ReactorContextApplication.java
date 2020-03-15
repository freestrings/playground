package fs.playground;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * curl localhost:8080/madatory?param=1\&name=n\&age=2
 * OK
 * <p>
 * curl localhost:8080/madatory?param=1\&name=n
 * Exception
 * <p>
 * curl localhost:8080/optional?param=1\&name=n
 * OK
 * <p>
 * curl localhost:8080/optional?param=1
 * OK
 */
@SpringBootApplication
public class ReactorContextApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReactorContextApplication.class, args);
    }

}
