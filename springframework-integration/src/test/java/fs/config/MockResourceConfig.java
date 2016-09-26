package fs.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@TestConfiguration
public class MockResourceConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry
                .addViewController("/mock/praha")
                .setViewName("forward:/mock/praha/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/mock/**")
                .addResourceLocations("file:src/test/resources/data/");
    }

}
