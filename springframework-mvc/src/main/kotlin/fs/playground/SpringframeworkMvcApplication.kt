package fs.playground

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
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
}