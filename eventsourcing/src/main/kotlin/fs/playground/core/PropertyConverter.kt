package fs.playground.core

import javax.persistence.AttributeConverter

class ClassConverter : AttributeConverter<Class<*>, String> {

    override fun convertToEntityAttribute(dbData: String?): Class<*> {
        return Class.forName(dbData)
    }

    override fun convertToDatabaseColumn(attribute: Class<*>?): String {
        return attribute!!.name
    }

}