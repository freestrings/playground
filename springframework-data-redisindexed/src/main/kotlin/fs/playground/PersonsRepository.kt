package fs.playground

import org.springframework.data.repository.CrudRepository

interface PersonsRepository : CrudRepository<Persons, Long>