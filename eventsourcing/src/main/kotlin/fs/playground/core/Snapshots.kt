package fs.playground.core

import org.springframework.beans.factory.annotation.Autowired
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

interface SnapshotRepository : JpaRepository<Snapshots, Long>, SnapshotRepositoryCustom

interface SnapshotRepositoryCustom {
    fun findTop1OrderByEventIdDesc(entityId: Long, entityType: Class<*>): Snapshots?
}

class SnapshotRepositoryImpl(
        @Autowired val entityManager: EntityManager
) : SnapshotRepositoryCustom {
    override fun findTop1OrderByEventIdDesc(entityId: Long, entityType: Class<*>): Snapshots? {
        val query = entityManager.createQuery("""
            select s from Snapshots s
            where s.entityId.entityId=:entityId
            and s.entityId.entityType=:entityType
            order by s.eventId desc
        """, Snapshots::class.java)
        return try {
            query.setParameter("entityId", entityId)
            query.setParameter("entityType", entityType)
            query.maxResults = 1
            query.singleResult
        } catch (e: NoResultException) {
            null
        }
    }
}
