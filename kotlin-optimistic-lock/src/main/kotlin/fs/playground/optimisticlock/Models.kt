package fs.playground.optimisticlock

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime
import javax.persistence.*

@Entity
data class Ticket(
        @Id val name: String,
        val maxium: Int,
        var updated: LocalDateTime,
        @Version val version: Int = 0
)

@Entity
data class TicketEvent(
        @Id @GeneratedValue var id: Long? = null,
        @Enumerated(EnumType.STRING) val eventType: TicketEventType,
        val ticketName: String,
        val payload: String
)
