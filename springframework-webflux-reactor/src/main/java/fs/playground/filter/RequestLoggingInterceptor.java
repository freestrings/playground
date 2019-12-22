package fs.playground.filter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.core.publisher.Flux;

@Getter
@Slf4j
public class RequestLoggingInterceptor extends ServerHttpRequestDecorator {

    private PayloadReader payloadReader = new PayloadReader();

    public RequestLoggingInterceptor(ServerHttpRequest delegate) {
        super(delegate);
    }

    @Override
    public Flux<DataBuffer> getBody() {
        return super.getBody().doOnNext(payloadReader.create());
    }
}
