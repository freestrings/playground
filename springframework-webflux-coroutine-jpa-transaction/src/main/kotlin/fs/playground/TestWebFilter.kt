package fs.playground

import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.util.context.Context
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@Component
class TestWebFilter() : WebFilter {

    val inc = AtomicInteger(0)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val value = exchange.request.queryParams["value"]?.let { it[0] } ?: "${inc.incrementAndGet()}".padEnd(
            5,
            padChar = '0'
        )
        return chain
            .filter(exchange)
            .subscriberContext(Context.of(AsyncFsContext.Key, AsyncFsContext(uuid = value)))
    }
}