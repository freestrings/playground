package fs;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource("/config/integration.xml")
public class IntegrationApp extends SpringBootServletInitializer {

    public static void main(String... args) {
        new SpringApplicationBuilder(IntegrationApp.class)
                .bannerMode(Banner.Mode.OFF)
                .run(args);

    }

}


