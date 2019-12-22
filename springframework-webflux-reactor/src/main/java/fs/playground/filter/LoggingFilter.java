package fs.playground.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        final ServerWebExchangeDecorator decorator = new ReadablePayloadExchangeDecorator(exchange);

        return chain.filter(decorator)
                .doFinally(signalType -> {

                    if (decorator.getRequest() instanceof RequestLoggingInterceptor) {
                        RequestLoggingInterceptor request = (RequestLoggingInterceptor) decorator.getRequest();
                        log.info("##### request: [{}] {} {} {}",
                                request.getMethod(),
                                request.getPath(),
                                request.getQueryParams().toString(),
                                request.getPayloadReader().getPayload()
                        );
                    }

                    if (decorator.getResponse() instanceof ResponseLoggingInterceptor) {
                        ResponseLoggingInterceptor response = (ResponseLoggingInterceptor) decorator.getResponse();
                        log.info("##### response: [{}] {}",
                                response.getStatusCode(),
                                response.getPayloadReader().getPayload()
                        );
                    }

                    log.info("web filter do finally: {}", signalType);
                });
    }

}
