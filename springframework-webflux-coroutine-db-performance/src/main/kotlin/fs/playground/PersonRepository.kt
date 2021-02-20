package fs.playground

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PersonRepository : CrudRepository<Person, Long> {

    @Query(nativeQuery = true, value = "SELECT id, name FROM person")
    fun findAllNative(): List<Person>

    @Query("SELECT p FROM person p")
    fun findAllJpql(): List<Person>
}