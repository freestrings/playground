package fs.playground.optimisticlock

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "ticket")
data class Ticket(
        @Id val name: String,
        val max: Int,
        var updated: LocalDateTime,
        @Version val version: Int = 0
)

@Entity
@Table(name = "ticket_event")
data class TicketEvent(
        @Id @GeneratedValue var id: Long? = null,
        @Enumerated(EnumType.STRING) @Column(name = "event_type") val eventType: TicketEventType,
        @Column(name = "ticket_name") val ticketName: String,
        val payload: String
)
