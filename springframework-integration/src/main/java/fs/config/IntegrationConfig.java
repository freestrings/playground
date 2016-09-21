package fs.config;

import fs.GatewayMessageConvertersionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;

@Configuration
public class IntegrationConfig {

    /**
     * @return
     * @see org.springframework.integration.support.utils.IntegrationUtils.getConversionService( BeanFactory beanFactory)
     */
    @Bean
    public ConversionService integrationConversionService() {
        return new GatewayMessageConvertersionService();
    }
}
