package fs.playground.entity

import com.fasterxml.jackson.databind.ObjectMapper
import fs.playground.LTCDispatcher
import javax.persistence.PrePersist
import javax.persistence.PreRemove
import javax.persistence.PreUpdate

class PersonListener {

    var mapper: ObjectMapper = ObjectMapper()

    @PreUpdate
    @PrePersist
    @PreRemove
    fun beforeUpdate(entity: Any) {
        println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(SaveBefore(uuid = LTCDispatcher.getUUID(), entity = entity)))
    }

    data class SaveBefore(
            val uuid: String?,
            val entity: Any,
            val type: Class<Any> = entity.javaClass,
    )
}