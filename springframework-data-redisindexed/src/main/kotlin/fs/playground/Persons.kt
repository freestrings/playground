package fs.playground

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash("persons")
data class Persons(
    @Id val cstno: Long,
    var c1: InnerPerson? = null,
    var c2: InnerPerson? = null,
    var c3: InnerPerson? = null,
    var c4: InnerPerson? = null,
    var c5: InnerPerson? = null,
    var c6: InnerPerson? = null,
    var c7: InnerPerson? = null,
    var c8: InnerPerson? = null,
    var c9: InnerPerson? = null,
    var c0: InnerPerson? = null,
)

data class InnerPerson(
    val cstno: Long,
    val stat: String,
)