package fs.playground

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class Ctrl(val reactivePersonRepository: ReactivePersonRepository) {

    @GetMapping("r2dbc")
    fun r2dbc(): Flux<Person> {
        return reactivePersonRepository.findAll()
    }

    @GetMapping("/loop")
    suspend fun deferredLoop(@RequestParam("count") count: Int): String {
        (0..count).map { r2dbc() }
        return "ok"
    }
}