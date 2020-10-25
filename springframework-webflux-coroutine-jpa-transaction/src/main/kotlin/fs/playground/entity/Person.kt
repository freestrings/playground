package fs.playground.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity(name = "person")
data class Person(
        @get:Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
        var name: String
)
