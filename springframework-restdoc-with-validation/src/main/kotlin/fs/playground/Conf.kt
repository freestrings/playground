package fs.playground

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class ZuulConf {

    @Bean
    fun wmpZuulFilter(): WmpZuulFilter = WmpZuulFilter()

}


class WmpZuulFilter : ZuulFilter() {

    val log: Logger = LoggerFactory.getLogger(WmpZuulFilter::class.java)

    override fun run(): Any? {
        val ctx = RequestContext.getCurrentContext()
        val request = ctx.getRequest()

        log.info(String.format("%s request to %s", request.getMethod(), request.getRequestURL().toString()))

        return null
    }

    override fun shouldFilter(): Boolean = true

    override fun filterType(): String = "pre"

    override fun filterOrder(): Int = 1

}
