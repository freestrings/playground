package fs.playground.entity

import javax.persistence.Entity
import javax.persistence.Id

@Entity(name = "person")
data class Person(@get:Id var id: Long, var name: String)
