package fs.playground.filter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Getter
@Slf4j
public class ResponseLoggingInterceptor extends ServerHttpResponseDecorator {

    private PayloadReader payloadReader = new PayloadReader();

    public ResponseLoggingInterceptor(ServerHttpResponse delegate) {
        super(delegate);
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> payload) {
        return super.writeWith(Flux.from(payload).doOnNext(payloadReader.create()));
    }
}
