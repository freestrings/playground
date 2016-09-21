package fs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GatewayMessageConvertersionService extends DefaultConversionService {

    private ObjectMapper objectMapper = new ObjectMapper();

    public GatewayMessageConvertersionService() {
        super();
        addConverts();
    }

    // FIXME 자동으로 안댐? 매번 등록? reflection?
    private void addConverts() {
        this.addConverter(String.class, MessageA.class, source -> transform(source, MessageA.class));
        this.addConverter(String.class, MessageB.class, source -> transform(source, MessageB.class));
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
