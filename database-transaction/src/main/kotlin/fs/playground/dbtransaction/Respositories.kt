package fs.playground.dbtransaction

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.EntityManager
import javax.persistence.LockModeType

interface TestTableRepository : JpaRepository<TestTable, String>, TestTableRepositoryExt

interface TestTableRepositoryExt {
    fun increase(id: String)
}

class TestTableRepositoryImpl(
        @Autowired val entityManager: EntityManager
) : TestTableRepositoryExt {

    override fun increase(id: String) {
        val query = entityManager.createQuery("select t from TestTable t where id = :name", TestTable::class.java)
        query.setParameter("name", id)
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE)
        val testTable: TestTable? = query.singleResult

        testTable?.let {
            if (testTable.count < testTable.countLimit) {
                testTable.count++
                println("${Thread.currentThread()} - ${testTable.count}")
                entityManager.persist(testTable)
            } else {
                throw Exception("넘었음")
            }
        }
    }

}