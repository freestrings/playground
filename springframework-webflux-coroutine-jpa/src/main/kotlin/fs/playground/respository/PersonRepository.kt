package fs.playground.respository

import fs.playground.entity.Person
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PersonRepository : CrudRepository<Person, Long>