package fs.playground.dbtransaction

import org.springframework.data.jpa.repository.JpaRepository

interface TestTableRepository: JpaRepository<TestTable, String>