package fs.playground.showcase

import javax.validation.constraints.Max
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class Dummy(@get:NotNull val strValue: String, @get:Max(value = 100) val intValue: Int, @get:Size(min = 1, max = 10) val arrayValue: Array<String>)