package fs.playground.repository

import fs.playground.entity.Person
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PersonRepository : CrudRepository<Person, Long> {
    fun countByName(name: String): Long
}