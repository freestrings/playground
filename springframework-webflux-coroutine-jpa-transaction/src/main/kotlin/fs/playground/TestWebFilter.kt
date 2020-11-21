package fs.playground

import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.util.context.Context
import java.util.*

@Component
class TestWebFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
//        return Mono.just(1)
//                .publishOn(Schedulers.elastic())
//                .flatMap {
//                    val uuid = UUID.randomUUID().toString()
//                    chain.filter(exchange)
//                            .subscriberContext(Context.of(AsyncFsContext.Key, AsyncFsContext(uuid)))
//                }

        val uuid = exchange.request.queryParams["id"]?.let { it[0] } ?: UUID.randomUUID().toString()
        return chain.filter(exchange).subscriberContext(Context.of(AsyncFsContext.Key, AsyncFsContext(uuid)))
    }
}