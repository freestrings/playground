package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Hello {

    @Autowired
    private BinaryLogClientCreator binaryLogClientCreator;

    public static void main(String... args) {
        SpringApplication.run(Hello.class, args);
    }

}
