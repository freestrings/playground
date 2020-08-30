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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

fun main(args: Array<String>) {
    val log = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
    val tableMeta = loadTableMeta()
    val sql = getSql16()
    log.debug(sql)
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

    private val log = LoggerFactory.getLogger(this::class.java)

    private val columns = Stack<MutableList<Map<String, String>>>()
    private val tables = Stack<MutableList<Map<String, String>>>()
    private var bindVariables = Stack<MutableList<String>>()
    private var bindContextes = Stack<MutableMap<String, MutableList<String>>>()
    private var bindColumnsOrdered = Stack<MutableList<Pair<String, String>>>()

    override fun onEvent(e: ParseEvent) {
        log.debug("## $e")

        when (e.event) {
            ParseEvent.Type.COLUMN -> {
                log.debug("-- column: ${e.data["columnName"]}, tableOrTableAlias: ${e.data["tableOrTableAlias"]}")

                columns.peek().add(e.data)
                val columnName = e.data["columnName"] as String
                val tableOrTableAliasOfColumn = e.data["tableOrTableAlias"]

                if (tableOrTableAliasOfColumn != null && tableOrTableAliasOfColumn.isNotEmpty()) {
                    val tableCandidates = tables.peek().mapNotNull { table ->
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
                        log.error("UnExpected: table notfound $e")
                        return
                    }

                    if (tableCandidates.size > 1) {
                        log.error("UnExpected: too many table found $e")
                        return
                    }

                    if (!isColumnExist(tableCandidates[0], columnName, tablesMeta)) {
                        log.error("Unknown column: $columnName of ${tableCandidates[0]}")
                    } else if (bindContextes.isNotEmpty()) {
                        keepBindContext(tableCandidates[0], columnName)
                    }
                } else {
                    if (tables.peek().size == 0) {
                        // from절이 sub select : ignore
                        return
                    }

                    if (tables.peek()
                            .filter { table ->
                                val tableName = table["name"] as String
                                val exist = isColumnExist(tableName, columnName, tablesMeta)
                                if (exist && bindContextes.isNotEmpty()) {
                                    keepBindContext(tableName, columnName)
                                }
                                exist
                            }.count() < 1
                    ) {
                        log.error("Unknown column: $columnName in ${tables.peek().map { it["name"] as String }} ")
                        return
                    }
                }
            }
            ParseEvent.Type.TABLE -> {
                log.debug("-- table: ${e.data["name"]}, alias: ${e.data["alias"]}")
                if (!tablesMeta.containsKey(e.data.getValue("name").toLowerCase())) {
                    log.error("Unknown table: ${e.data["name"]}, alias: ${e.data["alias"]}")
                    return
                }
                tables.peek().add(e.data)
            }
            ParseEvent.Type.NEW_CONTEXT -> {
                columns.push(mutableListOf())
                tables.push(mutableListOf())
            }
            ParseEvent.Type.END_CONTEXT -> {
                columns.pop()
                tables.pop()
            }
            ParseEvent.Type.BIND_VARIABLE -> {
                if (bindVariables.isNotEmpty()) {
                    bindVariables.peek().add(e.data.getValue("name"))
                } else {
                    println()
                }
            }
            ParseEvent.Type.NEW_BIND -> {
                bindVariables.push(mutableListOf())
                bindContextes.push(mutableMapOf())
                bindColumnsOrdered.push(mutableListOf())
            }
            ParseEvent.Type.END_BIND -> {
                val bindVariable = bindVariables.pop()
                val bindContext = bindContextes.pop()
                val bindColumnOrdered = bindColumnsOrdered.pop()

                for (i in 0 until bindVariable.size) {
                    try {
                        val bindColumn = bindColumnOrdered[i]
                        val column = bindContext.getValue(bindColumn.first)[0]
                        log.info("${bindVariable[i]} : $column of ${bindColumn.first}")
                    } catch (e: IndexOutOfBoundsException) {
                        log.info("${bindVariable[i]} : Unknown column")
                    }
                }
            }
            ParseEvent.Type.TRACE -> {
                log.trace("----$e")
            }
        }
    }

    private fun keepBindContext(tableName: String, columnName: String) {
        bindColumnsOrdered.peek().add(Pair(tableName, columnName))
        if (bindContextes.peek().containsKey(tableName)) {
            bindContextes.peek().getValue(tableName).add(columnName)
        } else {
            bindContextes.peek()[tableName] = mutableListOf(columnName)
        }
    }

}

data class ParseEvent(val event: Type, val data: Map<String, String>) {
    enum class Type {
        COLUMN,
        BIND_VARIABLE,
        TRACE,
        TABLE,
        NEW_CONTEXT,
        END_CONTEXT,
        NEW_BIND,
        END_BIND
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
        parseEventEmitter.emit(ParseEvent(ParseEvent.Type.TRACE, mapOf("trace" to "visit: subSelect: SubSelect?")))
        subSelect?.let { it.selectBody.accept(SimpleSelectVisitor(parseEventEmitter)) }
        parseEventEmitter.emit(ParseEvent(ParseEvent.Type.TRACE, mapOf("trace" to "visit-end: subSelect: SubSelect?")))
    }
}

class SimpleSelectVisitor(private val parseEventEmitter: ParseEventEmitter) : SelectVisitorAdapter() {

    override fun visit(plainSelect: PlainSelect?) {
        parseEventEmitter.emit(ParseEvent(ParseEvent.Type.TRACE, mapOf("trace" to "visit: plainSelect: PlainSelect?")))

        parseEventEmitter.emit(
            ParseEvent(
                ParseEvent.Type.NEW_CONTEXT,
                mapOf()
            )
        )

        plainSelect?.let { plainSelect ->
            val simpleExpressionVisitor = SimpleExpressionVisitor(parseEventEmitter)
            val simpleSelectItemVisitor = SimpleSelectItemVisitor(parseEventEmitter, simpleExpressionVisitor)

            plainSelect.fromItem.accept(SimpleFromItemVisitor(parseEventEmitter))
            plainSelect.joins?.let { joins ->
                joins.forEach { join ->
                    join.rightItem.accept(SimpleFromItemVisitor(parseEventEmitter))
                }
            }
            plainSelect.distinct?.let { distinct ->
                distinct.onSelectItems.forEach { it.accept(simpleSelectItemVisitor) }
            }
            plainSelect.selectItems?.let { selectItems ->
                selectItems.forEach { it.accept(simpleSelectItemVisitor) }
            }
            plainSelect.where?.let { it.accept(simpleExpressionVisitor) }
            plainSelect.joins?.let { joins ->
                joins.forEach { join ->
                    join.onExpression?.let { it.accept(simpleExpressionVisitor) }
                }
            }
        }

        parseEventEmitter.emit(
            ParseEvent(
                ParseEvent.Type.TRACE,
                mapOf("trace" to "visit-end: plainSelect: PlainSelect?")
            )
        )

        parseEventEmitter.emit(
            ParseEvent(
                ParseEvent.Type.END_CONTEXT,
                mapOf()
            )
        )
    }

    override fun visit(setOpList: SetOperationList?) {
        parseEventEmitter.emit(
            ParseEvent(
                ParseEvent.Type.TRACE,
                mapOf("trace" to "visit: setOpList: SetOperationList?")
            )
        )
        setOpList?.let { it.plainSelects.forEach(this::visit) }
    }
}

class SimpleSelectItemVisitor(
    private val parseEventEmitter: ParseEventEmitter,
    private val simpleExpressionVisitor: SimpleExpressionVisitor
) : SelectItemVisitorAdapter() {

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
            it.expression.accept(simpleExpressionVisitor)
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

open class SimpleExpressionVisitor(
    private val parseEventEmitter: ParseEventEmitter
) : ExpressionVisitorAdapter() {

    override fun visit(function: Function?) {
        function?.let { function ->
            function.parameters?.let { expressionList ->
                val hasBindVariable = expressionList.expressions?.let { expressions ->
                    expressions.filterIsInstance<JdbcNamedParameter>()
                }?.let { it.count() > 0 } ?: false

                if (hasBindVariable) {
                    parseEventEmitter.emit(ParseEvent(ParseEvent.Type.NEW_BIND, mapOf()))
                }
                expressionList.expressions?.let { expressions ->
                    expressions.forEach {
                        it.accept(this)
                    }
                }

                if (hasBindVariable) {
                    parseEventEmitter.emit(ParseEvent(ParseEvent.Type.END_BIND, mapOf()))
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

    private fun acceptExpr(expr: BinaryExpression?) {
        expr?.let {
            if (it.leftExpression is JdbcNamedParameter || it.rightExpression is JdbcNamedParameter) {
                parseEventEmitter.emit(ParseEvent(ParseEvent.Type.NEW_BIND, mapOf()))
            }

            it.leftExpression.accept(this)
            it.rightExpression.accept(this)

            if (it.leftExpression is JdbcNamedParameter || it.rightExpression is JdbcNamedParameter) {
                parseEventEmitter.emit(ParseEvent(ParseEvent.Type.END_BIND, mapOf()))
            }
        }
    }

    override fun visit(expr: InExpression?) {
        expr?.let {
            if (it.leftExpression is JdbcNamedParameter) {
                parseEventEmitter.emit(ParseEvent(ParseEvent.Type.NEW_BIND, mapOf()))
            }

            it.leftExpression.accept(this)

            if (it.leftExpression is JdbcNamedParameter) {
                parseEventEmitter.emit(ParseEvent(ParseEvent.Type.END_BIND, mapOf()))
            }
        }
    }

    override fun visit(expr: EqualsTo?) {
        acceptExpr(expr)
    }

    override fun visit(expr: GreaterThan?) {
        acceptExpr(expr)
    }

    override fun visit(expr: GreaterThanEquals?) {
        acceptExpr(expr)
    }

    override fun visit(expr: LikeExpression?) {
        acceptExpr(expr)
    }

    override fun visit(expr: MinorThan?) {
        acceptExpr(expr)
    }

    override fun visit(expr: MinorThanEquals?) {
        acceptExpr(expr)
    }

    override fun visit(expr: NotEqualsTo?) {
        acceptExpr(expr)
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
                    "fullyQualifiedName" to column!!.fullyQualifiedName
                )
            )
        )
    }
}

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
        where t1.a = (select min(c) from tab2)
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
        where t1.a in (select a from tab1) 
    """.trimIndent()
}

fun getSql5(): String? {
    return """
        select t1.*
            , case when (select count(*) from tab1) = 1 then 1 else 2 end 
        from 
            tab1 t1,
            tab2 t2 
        where t2.a = (case when (select count(*) from tab1) = 1 then 1 else 2 end)
    """.trimIndent()
}

fun getSql6(): String? {
    return """
        select * from tab1 t1 
        where t1.a = to_char(
                    (select count(a) from tab2),
                    ''
                )
    """.trimIndent()
}

fun getSql7(): String? {
    return """
        select * from tab1 t1 
        where exists (
            select a from tab2
            where t2.a = (select count(f) from tab3)
        )
    """.trimIndent()
}

fun getSql8(): String? {
    return """
        select * from tab1 t1 
        where t1.a like b || c || (select a from tab2)
    """.trimIndent()
}

fun getSql9(): String? {
    return """
        select * from tab1 t1 
        where t1.a = cast(:a as unsinged)
            and t1.b = cast( (select a from tab2) as unsigned )
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

fun getSql12(): String? {
    return """
        select a from tab1 t1,
            tab2 t2
        where :bind1 = (
                        select 
                            e 
                        from tab3
                            inner join tab1 tt1 on tt1.a = f  
                        where f = 1 and g = 2)
            and t2.c = :bind2
    """.trimIndent()
}

fun getSql13(): String? {
    return """
        select a from tab1 t1,
            tab2 t2
        where :bind1 = (
                        select 
                            e 
                        from tab3,
                            tab1 tt1,
                            tab2 tt2
                        where tt1.a = :bind2 and f = :bind3 and g = 2)
            and t2.c = :bind4
    """.trimIndent()
}

fun getSql14(): String? {
    return """
        select a from tab1 t1,
            tab2 t2
        where :bind1 = t2.a
            and t2.c = :bind2
    """.trimIndent()
}

fun getSql15(): String? {
    return """
        select a from tab1 t1,
            tab2 t2
        where :bind1 = (
                        select 
                            e 
                        from tab3,
                            tab1 tt1,
                            tab2 tt2
                        where tt1.a = :bind2 and f = :bind3 and g = 2)
            and t2.c = :bind4
            and t2.b = (
                        select a from tab1 t1
                            inner join tab2 t2 on t1.a = t2.a
                                and t2.a like concat(:bind5, '%%')
            )
    """.trimIndent()
}

fun getSql16(): String? {
    return """
        select a from tab1 t1,
            tab2 t2
        where :bind1 = (
                        select 
                            e 
                        from tab3,
                            tab1 tt1,
                            tab2 tt2
                        where tt1.a = :bind2 and f = :bind3 and g = 2)
            and t2.c = :bind4
            and :bind6 = (
                        select a from tab1 t1
                            inner join tab2 t2 on t1.a = t2.a
                                and t2.a like concat(:bind5, '%%')
            )
    """.trimIndent()
}