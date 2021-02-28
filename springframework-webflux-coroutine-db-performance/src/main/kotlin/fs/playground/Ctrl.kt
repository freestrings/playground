package fs.playground

import fs.playground.jpa.PersonRepository
import fs.playground.r2dbc.Person
import fs.playground.r2dbc.ReactivePersonRepository
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class Ctrl(val reactivePersonRepository: ReactivePersonRepository, val personRepository: PersonRepository) {

    @GetMapping("r2dbc")
    fun r2dbc(): Flow<Person> {
        return reactivePersonRepository.findAll().asFlow()
    }

    @GetMapping("jpa")
    suspend fun jpa(): Iterable<fs.playground.jpa.Person> {
        return personRepository.findAll()
    }

//    @GetMapping("/loop")
//    suspend fun deferredLoop(@RequestParam("count") count: Int, @RequestParam("type") type: String): String {
//        when(type) {
//            "r2dbc" -> {
//                val list = (0..count).map { r2dbc() }.toList()
//                awaitAll(*list.toTypedArray())
//            }
//            else -> (0..count).map { jpa() }
//        }
//
//        return "ok"
//    }
}