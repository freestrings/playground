package fs.playground.optimisticlock

import java.io.Serializable
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
@Table(name = "ticket_event", indexes = arrayOf(Index(name = "idx_ticket_name", columnList = "ticket_name")))
data class TicketEvent(
        @Id @GeneratedValue val id: Long = 0,
        @Column(name = "ticket_name") val ticketName: String,
        @Enumerated(EnumType.STRING) @Column(name = "event_type") val eventType: TicketEventType,
        val payload: String
)

@Embeddable
data class SnapshotId(
        @Column(name = "event_id") val eventId: Long,
        @Column(name = "ticket_name") val ticketName: String
) : Serializable

@Entity
@Table(name = "snapshot")
data class Snapshot(
        @EmbeddedId val id: SnapshotId,
        val count: Int
)