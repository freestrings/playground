package fs.config;

import fs.GatewayMessageConvertersionService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpMethod;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler;
import org.springframework.messaging.MessageChannel;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class IntegrationConfig {

    @Bean
    public MessageChannel inHttp() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel outHttp() {
        return new DirectChannel();
    }

    @Bean
    public GatewayMessageConvertersionService gatewayMessageConvertersionService(BeanFactory beanFactory) {
        return new GatewayMessageConvertersionService(beanFactory);
    }

    @Bean
    @ServiceActivator(inputChannel = "inHttp")
    HttpRequestExecutingMessageHandler httpGateway(@Value("${fs.outboundUrl}") String url) {

        HttpRequestExecutingMessageHandler gateway = new HttpRequestExecutingMessageHandler(url + "/{routePath}");
        gateway.setHttpMethod(HttpMethod.GET);
        gateway.setExpectedResponseType(String.class);

        SpelExpressionParser expressionParser = new SpelExpressionParser();
        Map<String, Expression> uriVariableExpressions = new HashMap<>();
        uriVariableExpressions.put("routePath", expressionParser.parseExpression("headers.routePath"));

        gateway.setUriVariableExpressions(uriVariableExpressions);
        return gateway;
    }

}
