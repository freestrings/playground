package fs.playground.optimisticlock

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.util.*
import java.util.concurrent.CompletableFuture

@RestController
class TicketController(@Autowired val ticketService: TicketService) {

    @PostMapping("/ticket")
    fun create(@RequestBody ticket: TicketCreate) = ticketService.create(ticket.name, ticket.max)

    @GetMapping("/ticket/{name}")
    fun consume(@PathVariable name: String): CompletableFuture<ConsumeType> {
        return ticketService.consumWithRetry(name, UUID.randomUUID().toString())
    }
}