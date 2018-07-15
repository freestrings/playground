package fs.playground.core

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "snapshots")
data class Snapshots(
        @Id @Column(name = "event_id") val eventId: Long = 0,
        @Column(name = "entity_id") val entityId: Long,
        @Column(name = "entity_type") val entityType: String,
        @Column(name = "snapshot_payload") val snapshotPayload: String
) {
    companion object {
        fun createSnapshot(eventId: Long, entityId: Long, entityType: String, snapshotPayload: String): Snapshots {
            return Snapshots(eventId = eventId, entityId = entityId, entityType = entityType, snapshotPayload = snapshotPayload)
        }
    }
}

interface SnapshotRepository : JpaRepository<Snapshots, Long>
