package fs.outbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import fs.outbound.config.OutboundInfo;
import fs.outbound.dto.ResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpMethod;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler;
import org.springframework.integration.support.utils.IntegrationUtils;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class OutboundConfig {

    @Autowired
    private OutboundInfo outboundInfo;

    @Bean
    public MessageChannel inHttp() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "inHttp")
    HttpRequestExecutingMessageHandler httpGateway() {

        Assert.isTrue(isValidUrl(), "Invalid Outbound URL: " + getUrl());

        HttpRequestExecutingMessageHandler gateway = new HttpRequestExecutingMessageHandler(
                getUrl(),
                new BasicAuthRestTemplate(outboundInfo.getUser(), outboundInfo.getPassword()));
        gateway.setHttpMethod(HttpMethod.GET);
        gateway.setExpectedResponseType(String.class);
        gateway.setUriVariableExpressions(getVariableExpressionMap());
        return gateway;
    }

    private boolean isValidUrl() {
        try {
            new URL(getUrl());
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private String getUrl() {
        return String.format("%s://%s:%d/%s/%s",
                outboundInfo.getProtocol(),
                outboundInfo.getHost(),
                outboundInfo.getPort(),
                outboundInfo.getPath(),
                "{routePath}");
    }

    private Map<String, Expression> getVariableExpressionMap() {
        SpelExpressionParser expressionParser = new SpelExpressionParser();
        Map<String, Expression> uriVariableExpressions = new HashMap<>();
        uriVariableExpressions.put("routePath", expressionParser.parseExpression("headers.routePath"));
        return uriVariableExpressions;
    }

    @Bean
    public OutboundMessageConvertersionService gatewayMessageConvertersionService(BeanFactory beanFactory) {
        return new OutboundMessageConvertersionService(beanFactory);
    }

    /**
     * @link fs.outbound.IOutboundService 리턴 타입 변환
     */
    class OutboundMessageConvertersionService {

        private ObjectMapper objectMapper = new ObjectMapper();

        public OutboundMessageConvertersionService(BeanFactory beanFactory) {
            ((GenericConversionService) IntegrationUtils.getConversionService(beanFactory))
                    .addConverter(String.class, ResponseDTO.class, source -> transform(source, ResponseDTO.class));
        }

        private <T> T transform(String source, Class<T> type) {
            try {
                return objectMapper.readValue(source, type);
            } catch (IOException e) {
                log.error("Convert to JSON", e);
                return null;
            }
        }
    }
}
