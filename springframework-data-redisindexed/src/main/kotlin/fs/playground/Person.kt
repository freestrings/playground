package fs.playground

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed

@RedisHash("person")
data class Person(
    @Id val pid: Long,
    @field:Indexed val cstno: Long,
    val stat: String
)