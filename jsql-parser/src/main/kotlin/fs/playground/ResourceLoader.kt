package fs.playground

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object ResourceLoader {

    fun loadMeta(resource: String): Map<String, Any> {
        val resource = Thread.currentThread().contextClassLoader.getResource(resource)
        val mapper = jacksonObjectMapper()
        val typeRef = object : TypeReference<Map<String, Any>>() {}
        return mapper.readValue(resource.openStream(), typeRef)
    }
}