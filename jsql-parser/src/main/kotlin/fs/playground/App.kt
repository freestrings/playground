package fs.playground

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.sf.jsqlparser.expression.*
import net.sf.jsqlparser.expression.Function
import net.sf.jsqlparser.expression.operators.relational.*
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.schema.Column
import net.sf.jsqlparser.schema.Table
import net.sf.jsqlparser.statement.select.*

fun main(args: Array<String>) {
    val tableMeta = loadTableMeta()
    val sql = getSql2()
//    println("input: $sql")
    val select = CCJSqlParserUtil.parse(sql) as Select
    val parseEventEmitter = ParseEventEmitter()
    parseEventEmitter.addLister(DefaultParseEventListener(tableMeta))
    select.selectBody.accept(SimpleSelectVisitor(parseEventEmitter))

}

fun loadTableMeta(): Map<String, Any> {
    val resource = Thread.currentThread().contextClassLoader.getResource("table.json")
    val mapper = jacksonObjectMapper()
    val typeRef = object : TypeReference<Map<String, Any>>() {}
    return mapper.readValue(resource.openStream(), typeRef)
}

fun isColumnExist(tableName: String, columnName: String, tablesMeta: Map<String, Any>): Boolean {
    if (columnName == "*") {
        return true
    }

    val tableMeta = tablesMeta[tableName.toLowerCase()] as Map<String, Any>?
    return tableMeta?.let { table ->
        val columns = table["columns"] as List<Map<String, String>>
        columns.any { it["name"] == columnName.toLowerCase() }
    } ?: false
}

class DefaultParseEventListener(private val tablesMeta: Map<String, Any>) : ParseEventListener {

    enum class Status(i: Int) {
        INIT(0),
        ON_TABLE(1),
        ON_COLUMN(2)
    }

    private val tables = mutableListOf<Map<String, String>>()
    private var status = Status.INIT

    override fun onEvent(e: ParseEvent) {
        println("## $e")
        when (e.event) {
            ParseEvent.Type.COLUMN -> {
//                println("-- column: ${e.data["columnName"]}, tableOrTableAlias: ${e.data["tableOrTableAlias"]}")
                status = Status.ON_COLUMN

                val columnName = e.data["columnName"] as String
                val tableOrTableAliasOfColumn = e.data["tableOrTableAlias"]

                if (tableOrTableAliasOfColumn != null && tableOrTableAliasOfColumn.isNotEmpty()) {
                    val tableCandidates = tables.mapNotNull { table ->
                        val tableName = table["name"] as String
                        val tableAlias = table["alias"]

                        when (tableOrTableAliasOfColumn) {
                            tableAlias -> {
                                tableName
                            }
                            tableName -> {
                                tableName
                            }
                            else -> {
                                null
                            }
                        }
                    }

                    if (tableCandidates.isEmpty()) {
                        error("UnExpected: table notfound")
                    }

                    if (tableCandidates.size > 1) {
                        error("UnExpected: too many table found")
                    }

                    if (!isColumnExist(tableCandidates[0], columnName, tablesMeta)) {
                        error("Unknown column: ${tableCandidates[0]}.$columnName")
                    }
                } else {
                    if (tables
                            .filter { table ->
                                val tableName = table["name"] as String
                                isColumnExist(tableName, columnName, tablesMeta)
                            }.count() < 1
                    ) {
                        error("Unknown column: $columnName in ${tables.map { it["name"] as String }} ")
                    }
                }
            }
            ParseEvent.Type.TABLE -> {
//                println("-- table: ${e.data["name"]}, alias: ${e.data["alias"]}")
                status = Status.ON_TABLE
                tables.add(e.data)
            }
            ParseEvent.Type.CLEAN_CONTEXT -> {
                tables.clear()
            }
            ParseEvent.Type.BIND_VARIABLE -> {
            }
            ParseEvent.Type.LOG -> {
//                println("----$e")
            }
        }
    }

}

data class ParseEvent(val event: Type, val data: Map<String, String>) {
    enum class Type {
        COLUMN,
        BIND_VARIABLE,
        LOG,
        TABLE,
        CLEAN_CONTEXT
    }
}

interface ParseEventListener {

    fun onEvent(e: ParseEvent)
}

class ParseEventEmitter {

    private val listeners = mutableListOf<ParseEventListener>()

    fun addLister(listener: ParseEventListener) {
        listeners.add(listener)
    }

    fun emit(e: ParseEvent) {
        listeners.forEach { it.onEvent(e) }
    }
}

class SimpleFromItemVisitor(private val parseEventEmitter: ParseEventEmitter) : FromItemVisitorAdapter() {
    override fun visit(table: Table?) {
        parseEventEmitter.emit(
            ParseEvent(
                ParseEvent.Type.TABLE,
                table?.let { table ->
                    mapOf(
                        "name" to table.name,
                        "alias" to (table.alias?.let { it.name } ?: "")
                    )
                } ?: mapOf()
            )
        )
    }

    override fun visit(subSelect: SubSelect?) {
        parseEventEmitter.emit(ParseEvent(ParseEvent.Type.LOG, mapOf("trace" to "visit: subSelect: SubSelect?")))
        subSelect?.let { it.selectBody.accept(SimpleSelectVisitor(parseEventEmitter)) }
        parseEventEmitter.emit(ParseEvent(ParseEvent.Type.LOG, mapOf("trace" to "visit-end: subSelect: SubSelect?")))
    }
}

class SimpleSelectVisitor(private val parseEventEmitter: ParseEventEmitter) : SelectVisitorAdapter() {

    override fun visit(plainSelect: PlainSelect?) {
        parseEventEmitter.emit(ParseEvent(ParseEvent.Type.LOG, mapOf("trace" to "visit: plainSelect: PlainSelect?")))

        plainSelect?.let { plainSelect ->
            val simpleSelectItemVisitor = SimpleSelectItemVisitor(parseEventEmitter)

            plainSelect.fromItem.accept(SimpleFromItemVisitor(parseEventEmitter))
            plainSelect.joins?.let { joins ->
                joins.forEach {
                    it.rightItem.accept(SimpleFromItemVisitor(parseEventEmitter))
                }
            }
            plainSelect.distinct?.let { distinct ->
                distinct.onSelectItems.forEach { it.accept(simpleSelectItemVisitor) }
            }
            plainSelect.selectItems?.let { selectItems ->
                selectItems.forEach { it.accept(simpleSelectItemVisitor) }
            }
            plainSelect.where?.let { it.accept(SimpleExpressionVisitor(parseEventEmitter)) }
        }

        parseEventEmitter.emit(
            ParseEvent(
                ParseEvent.Type.LOG,
                mapOf("trace" to "visit-end: plainSelect: PlainSelect?")
            )
        )

        parseEventEmitter.emit(
            ParseEvent(
                ParseEvent.Type.CLEAN_CONTEXT,
                mapOf()
            )
        )
    }

    override fun visit(setOpList: SetOperationList?) {
        parseEventEmitter.emit(ParseEvent(ParseEvent.Type.LOG, mapOf("trace" to "visit: setOpList: SetOperationList?")))
        setOpList?.let { it.plainSelects.forEach(this::visit) }
    }
}

class SimpleSelectItemVisitor(private val parseEventEmitter: ParseEventEmitter) : SelectItemVisitorAdapter() {

    override fun visit(columns: AllTableColumns?) {
        columns?.let {
            parseEventEmitter.emit(
                ParseEvent(
                    ParseEvent.Type.COLUMN,
                    mapOf("columnName" to "*", "tableOrTableAlias" to it.table.name)
                )
            )
        }
    }

    override fun visit(item: SelectExpressionItem?) {
        item?.let {
            it.expression.accept(SimpleExpressionVisitor(parseEventEmitter))
        }
    }

    override fun visit(columns: AllColumns?) {
        parseEventEmitter.emit(
            ParseEvent(
                ParseEvent.Type.COLUMN,
                mapOf("columnName" to "*")
            )
        )
    }
}

class SimpleExpressionVisitor(private val parseEventEmitter: ParseEventEmitter) : ExpressionVisitorAdapter() {

    override fun visit(function: Function?) {
        function?.let { function ->
            function.parameters?.let { expressionList ->
                expressionList.expressions?.let { expression ->
                    expression.forEach {
                        it.accept(this)
                    }
                }
            }
        }
    }

    override fun visit(parameter: JdbcNamedParameter?) {
        parseEventEmitter.emit(
            ParseEvent(
                ParseEvent.Type.BIND_VARIABLE,
                mapOf("name" to parameter!!.name)
            )
        )
    }

    override fun visit(expr: EqualsTo?) {
        expr?.let { it.leftExpression.accept(this) }
        expr?.let { it.rightExpression.accept(this) }
    }

    override fun visit(expr: GreaterThan?) {
        expr?.let { it.leftExpression.accept(this) }
        expr?.let { it.rightExpression.accept(this) }
    }

    override fun visit(expr: GreaterThanEquals?) {
        expr?.let { it.leftExpression.accept(this) }
        expr?.let { it.rightExpression.accept(this) }
    }

    override fun visit(expr: InExpression?) {
        expr?.let { it.rightItemsList.accept(this) }
    }

    override fun visit(expr: LikeExpression?) {
        expr?.let { it.rightExpression.accept(this) }
    }

    override fun visit(expr: MinorThan?) {
        expr?.let { it.leftExpression.accept(this) }
        expr?.let { it.rightExpression.accept(this) }
    }

    override fun visit(expr: MinorThanEquals?) {
        expr?.let { it.leftExpression.accept(this) }
        expr?.let { it.rightExpression.accept(this) }
    }

    override fun visit(expr: NotEqualsTo?) {
        expr?.let { it.leftExpression.accept(this) }
        expr?.let { it.rightExpression.accept(this) }
    }

    override fun visit(subSelect: SubSelect?) {
        subSelect?.let { it.accept(SimpleFromItemVisitor(parseEventEmitter)) }
    }

    override fun visit(expr: CaseExpression?) {
        expr?.let { caseExpression ->
            caseExpression.whenClauses?.let { whenClauses ->
                whenClauses.forEach { it.accept(this) }
            }
            caseExpression.elseExpression?.let { it.accept(this) }
        }
    }

    override fun visit(expr: WhenClause?) {
        expr?.let {
            it.whenExpression.accept(this)
            it.thenExpression.accept(this)
        }
    }

    override fun visit(expr: AllComparisonExpression?) {
        expr?.let { it.subSelect.accept(SimpleFromItemVisitor(parseEventEmitter)) }
    }

    override fun visit(expr: AnyComparisonExpression?) {
        expr?.let { it.subSelect.accept(SimpleFromItemVisitor(parseEventEmitter)) }
    }

    override fun visit(column: Column?) {
        parseEventEmitter.emit(
            ParseEvent(
                ParseEvent.Type.COLUMN,
                mapOf(
                    "columnName" to column!!.columnName,
                    "tableOrTableAlias" to if (column!!.fullyQualifiedName.indexOf('.') > -1) {
                        column!!.fullyQualifiedName.substring(0, column!!.fullyQualifiedName.indexOf('.'))
                    } else {
                        ""
                    },
                    "fullyQualifiedName" to column!!.fullyQualifiedName,
                    "inWhereCause" to "true"
                )
            )
        )
    }
}

/**
select items
subselect
setop list
select items
table
select items
table
 */
fun getSql1(): String? {
    return """
        select * from (
            select 
                *
            from 
                tab1 t1,
                tab2 t2
            where t1.a = t2.a
                and t1.b = t2.b(+)
                
            union all
            
            select 
                * 
            from 
                tab1 t3,
                tab2 t4
            where t3.a = t4.a
                and t3.b = t4.b(+)
        )
    """.trimIndent()
}

fun getSql1_1(): String? {
    return """
        select t1.a, t2.a from (
            select 
                *
            from 
                tab1 t1,
                tab2 t2
            where t1.a = t2.a
                and t1.b = t2.b(+)
                
            union all
            
            select 
                * 
            from 
                tab1 t3,
                tab2 t4
            where t3.a = t4.a
                and t3.b = t4.b(+)
        ) t1, 
        (
            select 
                *
            from 
                tab1 k1,
                tab2 k2
            where k1.a = k2.a
                and k1.b = k2.b(+)
                
            union all
            
            select 
                * 
            from 
                tab1 k3,
                tab2 k4
            where k3.a = k4.a
                and k3.b = k4.b(+)
        ) t2
        where t1.o = t2.o
    """.trimIndent()
}

fun getSql1_2(): String? {
    return """
        select t1.a, t2.a from (
            select 
                t1.*
            from 
                tab1 t1,
                tab2 t2
            where t1.a = t2.a
                and t1.b = t2.b(+)
                
            union all
            
            select 
                t2.* 
            from 
                tab1 t3,
                tab2 t4
            where t3.a = t4.a
                and t3.b = t4.b(+)
        ) t1, 
        (
            select 
                k1.a
            from 
                tab1 k1,
                tab2 k2
            where k1.a = k2.a
                and k1.b = k2.b(+)
                
            union all
            
            select 
                k4.b 
            from 
                tab1 k3,
                tab2 k4
            where k3.a = k4.a
                and k3.b = k4.b(+)
        ) t2
        where t1.o = t2.o
    """.trimIndent()
}

fun getSql2(): String? {
    return """
        select t1.c from tab1 t1
            inner join tab2 t2 on t1.a = t2.d
    """.trimIndent()
}

fun getSql2_1(): String? {
    return """
        select c from tab1
            inner join tab2 t2 on t1.a = tab1.a
    """.trimIndent()
}

fun getSql2_2(): String? {
    return """
        select * from 
            tab1 t1,
            tab2 t2 
        where t1.a = t2.a(+)
            and t1.a(+) = t2.a
    """.trimIndent()
}

fun getSql3(): String? {
    return """
        select * from 
            tab1 t1,
            tab2 t2 
        where t1.a = (select min(c) from t3)
    """.trimIndent()
}

fun getSql3_1(): String? {
    return """
        select t1.a, t2.a, tab2.a from 
            tab1 t1,
            tab2 t2 
        where t1.a = (
                select min(c) from (
                    select count(*) as c from t3 
                    union all
                    select count(*) as c from t4
                )
        )
    """.trimIndent()
}

fun getSql4(): String? {
    return """
        select * from 
            tab1 t1,
            tab2 t2 
        where t1.a in (select a from t3) 
    """.trimIndent()
}

fun getSql5(): String? {
    return """
        select t1.*
            , case when (select count(*) from t4) = 1 then 1 else 2 end 
        from 
            tab1 t1,
            tab2 t2 
        where t2.a = (case when (select count(*) from t3) = 1 then 1 else 2 end)
    """.trimIndent()
}

fun getSql6(): String? {
    return """
        select * from tab1 t1 
        where t1.a = to_char(
                    (select count(a) from t2),
                    ''
                )
    """.trimIndent()
}

fun getSql7(): String? {
    return """
        select * from tab1 t1 
        where exists (
            select a from t2
            where t2.a = (select count(*) from t3)
        )
    """.trimIndent()
}

fun getSql8(): String? {
    return """
        select * from tab1 t1 
        where t1.a like a || b || (select a from t2)
    """.trimIndent()
}

fun getSql9(): String? {
    return """
        select * from tab1 t1 
        where t1.a = cast(:a as unsinged)
            and t1.b = cast( (select a from t2) as unsigned )
    """.trimIndent()
}

fun getSql10(): String? {
    return """
        select a, b, c as c1, t1.*, tab1.* from tab1 t1
    """.trimIndent()
}

fun getSql11(): String? {
    return """
        select a, b, c as c1, t1.*, tab1.* from tab1 t1
            inner join tab2 t2 on t1.a = t2.a
                and t1.b = t2.b
        where t1.z = (select i from tab3)
    """.trimIndent()
}