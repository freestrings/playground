package fs.playground.optimisticlock

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TicketController(@Autowired val ticketService: TicketService) {

    @GetMapping("/tickets")
    fun tickets(): ResponseEntity<MutableIterable<Ticket>> =
            ResponseEntity.ok(ticketService.findTicketAll())

    @PostMapping("/ticket")
    fun create(@RequestBody ticket: TicketCreate) =
            ResponseEntity.ok(ticketService.creatTicket(ticket.name, ticket.maxium))

    @PostMapping("/reservation")
    fun reservation(@RequestBody reservation: Reservation): ResponseEntity<Void> {
        val ret = ticketService.reservationTicket(reservation.ticketName, 0)
        return when (ret) {
            ReservationType.DONE -> ResponseEntity.status(HttpStatus.CREATED)
            ReservationType.FIAL -> ResponseEntity.status(HttpStatus.ACCEPTED)
            ReservationType.OVER -> ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
        }.build()
    }
}