package fs.playground.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;

public class ReadablePayloadExchangeDecorator extends ServerWebExchangeDecorator {

    private RequestLoggingInterceptor requestLoggingInterceptor;
    private ResponseLoggingInterceptor responseLoggingInterceptor;

    protected ReadablePayloadExchangeDecorator(ServerWebExchange delegate) {
        super(delegate);
    }

    @Override
    public ServerHttpRequest getRequest() {
        if (requestLoggingInterceptor == null) {
            requestLoggingInterceptor = new RequestLoggingInterceptor(super.getRequest());

        }
        return requestLoggingInterceptor;
    }

    @Override
    public ServerHttpResponse getResponse() {
        if (responseLoggingInterceptor == null) {
            responseLoggingInterceptor = new ResponseLoggingInterceptor(super.getResponse());
        }
        return responseLoggingInterceptor;
    }
}
