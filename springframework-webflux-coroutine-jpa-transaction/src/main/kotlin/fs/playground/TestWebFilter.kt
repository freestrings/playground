package fs.playground

import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.util.context.Context
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class TestWebFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val uuid = exchange.request.headers.getFirst("uuid")
                ?.let { it }
                ?: LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm::ss"))
        return chain.filter(exchange).subscriberContext(Context.of("uuid", uuid))
    }
}