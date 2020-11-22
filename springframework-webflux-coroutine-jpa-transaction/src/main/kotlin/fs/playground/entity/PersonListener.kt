package fs.playground.entity

import com.fasterxml.jackson.databind.ObjectMapper
import fs.playground.AsyncFsContext
import javax.persistence.PrePersist
import javax.persistence.PreRemove
import javax.persistence.PreUpdate

class PersonListener {

    var mapper: ObjectMapper = ObjectMapper()

    @PreUpdate
    @PrePersist
    @PreRemove
    fun beforeUpdate(entity: Any) {
        val p = entity as Person
        println("${AsyncFsContext.CTX.getUuid()} - ${p.name} ${p.name.contains(AsyncFsContext.CTX.getUuid()!!)} ${Thread.currentThread()}")
    }

    data class SaveBefore(
            val uuid: String?,
            val entity: Any,
            val type: Class<Any> = entity.javaClass,
    )
}