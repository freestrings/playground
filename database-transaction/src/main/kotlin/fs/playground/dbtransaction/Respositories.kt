package fs.playground.dbtransaction

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import java.sql.Connection
import java.sql.ResultSet
import javax.persistence.EntityManager
import javax.persistence.LockModeType
import javax.sql.DataSource

interface TestTableRepository : JpaRepository<TestTable, String>, TestTableRepositoryExt

interface TestTableRepositoryExt {
    fun increase(id: String)
    fun increase2(id: String)
}

class TestTableRepositoryImpl(
        @Autowired val entityManager: EntityManager,
        @Autowired val dataSource: DataSource
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

    override fun increase2(id: String) {
        val conn = dataSource.connection
        conn.use {
            conn.autoCommit = false
            conn.transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED
            val getCount = conn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY
            )

            getCount.use {
                if (!getCount.execute("select id, count, count_limit from test_table where id = '${id}' for update")) {
                    throw Exception("음?")
                }

                val rs = getCount.resultSet
                rs.use {
                    rs.first()
                    val count = rs.getInt("count")
                    val countLimit = rs.getInt("count_limit")
                    if (count < countLimit) {
                        conn.createStatement().use {
                            it.execute("update test_table set count = ${(count + 1)} where id = '${id}'")
                        }
                        conn.commit()
                    } else {
                        throw Exception("넘었음")
                    }
                }
            }
            conn.autoCommit = true
        }
    }
}