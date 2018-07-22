package fs.playground.core

import org.springframework.data.jpa.repository.JpaRepository
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

@Embeddable
data class EntityId(
        @Column(name = "entity_id") val entityId: Long,
        @Convert(converter = ClassConverter::class) @Column(name = "entity_type") val entityType: Class<*>
) : Serializable

/**
 * TODO FIX EmbededId에서 GeneratedValue가 안된다.
 */
@Entity
@Table(name = "entities",
        indexes = arrayOf(Index(name = "idx_entity_type", columnList = "entity_type"))
)
data class Entities(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "entity_id") val entityId: Long = 0,
        @Convert(converter = ClassConverter::class) @Column(name = "entity_type") val entityType: Class<*>,
        var updated: LocalDateTime = LocalDateTime.now(),
        @Column(name = "entity_payload") var entityPayload: String,
        @Version val version: Long = 0
) {
    companion object {
        fun create(entityType: Class<*>, entityPayload: String): Entities {
            return Entities(entityType = entityType, entityPayload = entityPayload)
        }
    }
}

interface EntityRepository : JpaRepository<Entities, Long>
