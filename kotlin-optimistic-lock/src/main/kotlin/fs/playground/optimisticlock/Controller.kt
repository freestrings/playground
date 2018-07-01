package fs.playground.optimisticlock

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.CompletableFuture

@RestController
class TicketController(@Autowired val ticketService: TicketService) {

    @PostMapping("/ticket")
    fun create(@RequestBody ticket: TicketCreate) = ticketService.create(ticket.name, ticket.maxium)

    @PostMapping("/ticket/{name}")
    fun consume(@PathVariable name: String): CompletableFuture<ConsumeType> = ticketService.consumWithRetry(name)
}