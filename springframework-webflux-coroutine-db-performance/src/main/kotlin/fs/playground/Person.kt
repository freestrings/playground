package fs.playground

import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

data class Person(
    @get:Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
    var name: String
)