package fs.playground

import org.springframework.data.repository.CrudRepository

interface PersonRepository : CrudRepository<Person, Long> {

    fun findAllByCstno(cstno: Long): List<Person>
}