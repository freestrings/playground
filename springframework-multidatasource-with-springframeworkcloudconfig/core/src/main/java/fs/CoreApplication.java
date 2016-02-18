package fs;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@SpringBootApplication
@RefreshScope
public class CoreApplication {

    public static void main(String... args) {
        new SpringApplicationBuilder(CoreApplication.class)
                .web(false)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
    }

}
