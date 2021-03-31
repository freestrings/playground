package fs.playground

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class Ctrls(val services: Services) {

    @GetMapping("/person")
    fun personInsert(@RequestParam seq: Long) {
        services.personInsert(seq)
    }

    @GetMapping("/animal")
    fun animalInsert(@RequestParam seq: Long) {
        services.animalInsert(seq)
    }
}