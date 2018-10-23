package fs.playground

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.validation.constraints.NotNull

@SpringBootApplication
class SpringframeworkMvcApplication

fun main(args: Array<String>) {
    runApplication<SpringframeworkMvcApplication>(*args)
}

@Service
@Validated
class ValidationAnnotationTest {

    fun check(@NotNull name: String?) {
        println("####${name}")
    }
}

@RestController
class Ctrl(@Autowired val validationAnnotationTest: ValidationAnnotationTest) {

    @GetMapping("/notnull")
    fun validationAnnotationTest() {
        validationAnnotationTest.check(null)
    }

    @GetMapping("/testa")
    fun testaAttribute(request: HttpServletRequest) {
        println(request.getAttribute("testa"))
    }

    @GetMapping("/testb")
    fun testbAttribute(request: HttpServletRequest) {
        println(request.getAttribute("testa"))
    }
}

@Configuration
class InterceptorConfig(private val interceptorTest: InterceptorTest) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        super.addInterceptors(registry)
        registry.addInterceptor(interceptorTest)
    }
}

@Component
class InterceptorTest : HandlerInterceptorAdapter() {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (request.requestURI.startsWith("/testa")) {
            request.setAttribute("testa", true);
        }
        return true
    }
}