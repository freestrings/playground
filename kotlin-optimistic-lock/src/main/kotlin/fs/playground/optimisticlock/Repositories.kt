package fs.playground.optimisticlock

import org.springframework.data.repository.CrudRepository

interface TicketRepository : CrudRepository<Ticket, String>

interface TicketEventRepository : CrudRepository<TicketEvent, Long> {

    fun findByTicketName(ticketName: String): MutableList<TicketEvent>
}