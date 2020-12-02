package fs.playground.entity

import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.Id

@EntityListeners(PersonListener::class)
@Entity(name = "person")
data class Person(
        @Id var id: String = "",
        var name: String = ""
)
