package fs.playground.core

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
@Table(name = "snapshots")
data class Snapshots(
        @Id @Column(name = "event_id") val eventId: Long = 0,
        @Embedded val entityId: EntityId,
        @Column(name = "snapshot_payload") val snapshotPayload: String
) {
    companion object {
        fun create(eventId: Long, entityId: Long, entityType: Class<*>, snapshotPayload: String): Snapshots {
            return Snapshots(
                    eventId = eventId,
                    entityId = EntityId(entityId = entityId, entityType = entityType),
                    snapshotPayload = snapshotPayload)
        }
    }
}

interface SnapshotRepository : JpaRepository<Snapshots, Long>
