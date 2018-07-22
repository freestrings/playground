package fs.playground.core

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
@Table(name = "events",
        indexes = arrayOf(Index(name = "idx_entity", columnList = "entity_id,entity_type"))
)
data class Events(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "event_id") val eventId: Long = 0,
        @Embedded val entityId: EntityId,
        @Column(name = "event_type") val eventType: String,
        @Column(name = "event_payload") val eventPayload: String
) {
    companion object {
        fun <T : Enum<T>> create(entityId: Long, entityType: Class<*>, eventType: T, eventPayload: String): Events {
            return Events(
                    entityId = EntityId(entityId = entityId, entityType = entityType),
                    eventType = eventType.name,
                    eventPayload = eventPayload)
        }
    }
}

interface EventRepository : JpaRepository<Events, Long> {
    fun findAllByEntityId(entityId: EntityId): List<Events>
    fun findTop10ByEntityIdOrderByEventIdDesc(entityId: EntityId): List<Events>
    fun findAllByEventIdGreaterThanAndEntityIdOrderByEventIdAsc(eventId: Long, entityId: EntityId): List<Events>
}