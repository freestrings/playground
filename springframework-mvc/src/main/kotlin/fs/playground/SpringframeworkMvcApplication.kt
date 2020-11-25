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

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class SpringframeworkMvcApplication

fun main(args: Array<String>) {
    runApplication<SpringframeworkMvcApplication>(*args)
}

@RestController
class Ctrl {

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
