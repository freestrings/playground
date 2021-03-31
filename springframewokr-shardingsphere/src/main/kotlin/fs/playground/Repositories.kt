package fs.playground

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AnimalRepository : CrudRepository<Animal, Long>

@Repository
interface PersonRepository : CrudRepository<Person, Long>