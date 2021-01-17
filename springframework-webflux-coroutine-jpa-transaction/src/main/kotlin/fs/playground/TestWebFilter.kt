package fs.playground

import fs.playground.event.TestaEvent
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.util.context.Context
import java.util.*

@Component
class TestWebFilter(val publisher: TestaEventPublisher) : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val asyncFsContext = AsyncFsContext()
        return chain
            .filter(exchange)
            .doFinally {
                if (exchange.request.uri.path == "/ctx") {
                    val data = asyncFsContext.getAllData()
                    if (data.isEmpty() == null) {
                        throw Exception("????")
                    }
                    println("filter $data - ${Thread.currentThread()}")
                    publisher.publish(TestaEvent(value = data))
                }
            }
            .subscriberContext(Context.of(AsyncFsContext.Key, asyncFsContext))
    }
}