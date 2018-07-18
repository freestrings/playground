package fs.playground.core

import org.springframework.data.jpa.repository.JpaRepository
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

@Embeddable
data class EntityId(
        @GeneratedValue @Column(name = "entity_id") val entityId: Long = 0,
        @Convert(converter = ClassConverter::class) @Column(name = "entity_type") val entityType: Class<*>
) : Serializable

@Entity
@Table(name = "entities")
data class Entities(
        @Id @Embedded val id: EntityId,
        var updated: LocalDateTime = LocalDateTime.now(),
        @Column(name = "entity_payload") var entityPayload: String,
        @Version val version: Long = 0
) {
    companion object {
        fun create(entityType: Class<*>, entityPayload: String): Entities {
            return Entities(id = EntityId(entityType = entityType), entityPayload = entityPayload)
        }
    }
}

interface EntityRepository : JpaRepository<Entities, EntityId>
