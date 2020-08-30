package fs.playground

class BindValueProvider(private val meta: Map<String, Any>) {
    fun get(tableName: String, columnName: String, data: Map<String, Any>?): List<String> {
        val defaultBindInfo = getBindInfo(tableName, columnName)
        when (defaultBindInfo.type) {
            "sql" -> {
                val map: MutableMap<String, Any> = mutableMapOf("table" to tableName, "column" to columnName)
                data?.let {
                    map.putAll(it)
                }
                return sql(map)
            }
            "list" -> {
                return defaultBindInfo.value.split(",").map { it.trim() }
            }
            "const" -> {
                return listOf(defaultBindInfo.value)
            }
            else -> {
                throw IllegalStateException("not supported type: ${defaultBindInfo.type}")
            }
        }
    }

    private fun sql(data: Map<String, Any>): List<String> {
        return emptyList()
    }

    private fun getBindInfo(tableName: String, columnName: String): DefaultBindInfo {
        if (meta.containsKey(tableName)) {
            val table = meta.getValue(tableName)
            val data = table as Map<String, Any>
            val columns = data.getValue("columns") as List<Map<String, String>>
            val column = columns.firstOrNull() { column ->
                column.getValue("name") == columnName
            }
            if (column != null) {
                return DefaultBindInfo(tableName, columnName, column.getValue("type"), column.getValue("value"))
            }
        }
        return DefaultBindInfo(tableName, columnName, "sql", "select distinct :column from :table")
    }
}

data class DefaultBindInfo(
    val tableName: String,
    val columnName: String,
    val type: String,
    val value: String
)