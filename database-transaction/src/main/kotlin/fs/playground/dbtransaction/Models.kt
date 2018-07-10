package fs.playground.dbtransaction

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "test_table")
data class TestTable(
        @Id val id: String,
        var count: Int,
        @Column(name = "column_limit") val countLimit: Int
)