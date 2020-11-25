package fs.playground.entity

import javax.persistence.*

@Entity(name = "person")
data class Person(
        @get:Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
        var name: String
)
