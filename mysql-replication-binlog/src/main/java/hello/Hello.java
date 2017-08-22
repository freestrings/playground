package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Hello {

    @Autowired
    private BinaryLogProcessor binaryLogProcessor;

    public static void main(String... args) {
        SpringApplication.run(Hello.class, args);
    }

}
