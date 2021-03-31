package fs.playground

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity(name = "person")
data class Person(
        @Id
        @Column(name = "person_seq")
        val personSeq: Long,
        @Column(name = "person_type")
        val personType: String,
        @Column(name = "person_name")
        val personName: String,
)

@Entity(name = "animal")
data class Animal(
        @Id
        @Column(name = "animal_seq")
        val animalSeq: Long,
        @Column(name = "animal_type")
        val animalType: String,
        @Column(name = "animal_name")
        val animalName: String,
)

@Entity(name = "person_type")
data class PersonType(
        @Id val type: String,
        val desc: String
)

