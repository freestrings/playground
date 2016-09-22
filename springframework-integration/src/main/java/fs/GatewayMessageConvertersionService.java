package fs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.integration.support.utils.IntegrationUtils;

import java.io.IOException;

public class GatewayMessageConvertersionService {

    public GatewayMessageConvertersionService(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        addConverts();
    }

    private BeanFactory beanFactory;

    private ObjectMapper objectMapper = new ObjectMapper();

    // FIXME 자동으로 안댐? 매번 등록? reflection?
    private void addConverts() {
        GenericConversionService conversionService = (GenericConversionService) IntegrationUtils.getConversionService(beanFactory);
        conversionService.addConverter(String.class, MessageA.class, source -> transform(source, MessageA.class));
        conversionService.addConverter(String.class, MessageB.class, source -> transform(source, MessageB.class));
    }

    private <T> T transform(String source, Class<T> type) {
        try {
            return objectMapper.readValue(source, type);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
