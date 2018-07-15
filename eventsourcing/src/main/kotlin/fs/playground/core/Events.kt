package fs.playground.core

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
@Table(name = "events",
        indexes = arrayOf(Index(name = "idx_entity", columnList = "entity_id,entity_type"))
)
data class Events(
        @Id @GeneratedValue @Column(name = "event_id") val eventId: Long = 0,
        @Embedded val entityId: EntityId,
        @Column(name = "event_type") val eventType: String,
        @Column(name = "event_payload") val eventPayload: String
) {
    companion object {
        fun createEvent(entityId: Long, entityType: String, eventType: String, eventPayload: String): Events {
            return Events(
                    entityId = EntityId(entityId = entityId, entityType = entityType),
                    eventType = eventType,
                    eventPayload = eventPayload)
        }
    }
}

interface EventRepository : JpaRepository<Events, Long> {
    fun findAllByEntityId(entityId: EntityId): List<Events>
}
