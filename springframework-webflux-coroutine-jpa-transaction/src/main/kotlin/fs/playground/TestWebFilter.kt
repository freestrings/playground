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
        return Mono.just(true)
                /**
                 * epoll 스레드에서 JPA가 사용되지 않게!
                 */
                .publishOn(Schedulers.boundedElastic())
                .flatMap {
                    val uuid = exchange.request.queryParams["id"]?.let { it[0] } ?: UUID.randomUUID().toString()
                    /**
                     *
                     * 1. asAsync나 withSlave를 사용하면 uuid가 전달되기 때문에 문제 없지만,
                     * 컨트롤러에서 asAsync나 withSlave 없이 바로 CUD 하면 PersonListener에서 uuid를 알 수 없기 때문에
                     * 여기에서 uuid값을 넣어준다.
                     *
                     * 2. 코루틴 context가 바뀔 때마다 여기 AsyncFsContext 생성자로 넘긴 uuid가
                     * 로직 실행전에 current 쓰레드로 전달 되기 때문에 uuid가 잘못 전달되는 문제는 없다.
                     * 그리고 uuid를 remove 하지않지만 uuid는 CTX의 ThreadLocal에 저장 되고
                     * boundedElastic 쓰레드는 사용후 60초가 지나면 사라지기 때문에 메모리릭은 없다.
                     */
                    AsyncFsContext.CTX.setUuid(uuid)
                    chain.filter(exchange)
                            .subscriberContext(Context.of(AsyncFsContext.Key, AsyncFsContext(uuid)))
                }
//        return chain.filter(exchange)
    }
}