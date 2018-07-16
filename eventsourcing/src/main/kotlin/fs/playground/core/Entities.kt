package fs.playground.core

import org.springframework.data.jpa.repository.JpaRepository
import java.io.Serializable
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
        @Version val version: Long = 0
) {
    companion object {
        fun create(entityType: Class<*>): Entities {
            return Entities(id = EntityId(entityType = entityType))
        }
    }
}

interface EntityRepository: JpaRepository<Entities, EntityId>
