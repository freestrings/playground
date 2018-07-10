package fs.playground.dbtransaction

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Component
class TestaService(
        @Autowired private val repository: TestTableRepository,
        @Autowired private val trasactionManager: JpaTransactionManager
) {

    fun testWithoutTransaction(id: String) {
        val testTable: TestTable? = repository.getOne(id)
        testTable?.let {
            if (testTable.count < testTable.countLimit) {
                testTable.count++
                println("${Thread.currentThread()} - ${testTable.count}")
                repository.save(testTable)
            } else {
                throw Exception("넘었음")
            }
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun testWithTransaction(id: String) {
//        val txDef = DefaultTransactionDefinition()
//        val txStatus = trasactionManager.getTransaction(txDef)
        val testTable: TestTable? = repository.getOne(id)
        testTable?.let {
            if (testTable.count < testTable.countLimit) {
                testTable.count++
                println("${Thread.currentThread()} - ${testTable.count}")
                repository.save(testTable)
            } else {
                throw Exception("넘었음")
            }
        }
//        trasactionManager.commit(txStatus)
    }

}