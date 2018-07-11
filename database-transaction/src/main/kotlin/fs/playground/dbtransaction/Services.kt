package fs.playground.dbtransaction

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class TestaService(
        @Autowired private val repository: TestTableRepository
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

    /**
     * https://stackoverflow.com/questions/30776518/jpa-difference-between-transaction-isolation-and-entity-locking
     *
     * Introduction
     *
     * There are different locking types and isolation levels. Some of the locking types (OPTIMISTIC*) are implemented on the JPA-level
     * (eg. in EclipseLink or Hibernate), and other (PESSIMISTIC*) are delegated by the JPA-provider to the DB level.
     *
     * Explanation
     *
     * Isolation levels and locking are not the same, but they may intersect somewhere.
     * If you have the SERIALIZED isolation level (which is performance-greedy), then you do not need any locking to do in JPA, as it is already done by the DB.
     * On the other side, if you choose READ_COMMITTED, then you may need to make some locking, as the isolation level alone will not guarantee you
     * e.g that the entry is not changed in the meanwhile by another transaction.
     *
     *
     * ### EntityManager Lock 없이 ###
     *
     * @org.springframework.transaction.annotation.Transactional(isolation = Isolation.SERIALIZABLE)
     * => org.hibernate.exception.LockAcquisitionException: could not execute statement
     * => com.mysql.jdbc.exceptions.jdbc4.MySQLTransactionRollbackException: Deadlock found when trying to get lock; try restarting transaction
     *
     * @org.springframework.transaction.annotation.Transactional(isolation = Isolation.REPEATABLE_READ)
     * @org.springframework.transaction.annotation.Transactional(isolation = Isolation.READ_COMMITTED)
     * => 에러 없음, 그러나 중복 업데이트 됨
     *
     *
     * ### EntityManager Lock 적용 ###
     *
     * 트랜잭션 시작 하지 않으면 setLockMode는 에러남
     * org.springframework.transaction.annotation.Transactional든 javax.transaction.Transactional 둘다 됨
     **/
    @javax.transaction.Transactional
    fun testWithTransaction(id: String) = repository.increase(id)

    fun testWithTransaction2(id: String) = repository.increase2(id)

}