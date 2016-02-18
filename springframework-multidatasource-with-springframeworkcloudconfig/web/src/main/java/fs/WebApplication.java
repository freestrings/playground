package fs;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class WebApplication {

    public static void main(String... args) {
        new SpringApplicationBuilder(WebApplication.class)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
    }
}
