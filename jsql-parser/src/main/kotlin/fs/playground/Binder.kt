package fs.playground

import net.sf.jsqlparser.expression.*
import net.sf.jsqlparser.expression.Function
import net.sf.jsqlparser.expression.operators.relational.*
import net.sf.jsqlparser.schema.Column
import net.sf.jsqlparser.schema.Table
import net.sf.jsqlparser.statement.select.*
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.util.*

data class BindMeta(
    val bindName: String,
    val columnName: String,
    val tableName: String
)

data class ErrorMeta(
    val name: String,
    val message: Map<String, String>
)

class InvalidSqlException(override val message: String, val errors: List<ErrorMeta>) : Exception(message)

class DefaultParseEventListener(
    private val tablesMeta: Map<String, Any>,
    private val errors: MutableList<ErrorMeta>,
    private val binds: MutableList<BindMeta>
) : ParseEventListener {

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
                        errors.add(ErrorMeta("TABLE_NOTFOUND", e.data))
                        return
                    }

                    if (tableCandidates.size > 1) {
                        log.error("UnExpected: too many table found $e")
                        errors.add(ErrorMeta("TOO_MANY_TABLE_FOUND", e.data))
                        return
                    }

                    if (!isColumnExist(tableCandidates[0], columnName, tablesMeta)) {
                        log.error("Unknown column: $columnName of ${tableCandidates[0]}")
                        errors.add(
                            ErrorMeta(
                                "UNKNOWN_COLUMN", mapOf(
                                    "column" to columnName,
                                    "table" to tableCandidates[0]
                                )
                            )
                        )
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
                        errors.add(
                            ErrorMeta(
                                "UNKNOWN_COLUMN", mapOf(
                                    "column" to columnName,
                                    "tables" to tables.peek().map { it["name"] as String }.toString()
                                )
                            )
                        )
                        return
                    }
                }
            }
            ParseEvent.Type.TABLE -> {
                log.debug("-- table: ${e.data["name"]}, alias: ${e.data["alias"]}")
                if (!tablesMeta.containsKey(e.data.getValue("name").toLowerCase())) {
                    log.error("Unknown table: ${e.data["name"]}, alias: ${e.data["alias"]}")
                    errors.add(
                        ErrorMeta(
                            "UNKNOWN_TABLE", mapOf(
                                "name" to e.data.getValue("name"),
                                "alias" to e.data.getValue("alias")
                            )
                        )
                    )
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
                    log.warn("Unexpected condition")
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
                        log.debug("${bindVariable[i]} : $column of ${bindColumn.first}")
                        binds.add(BindMeta(bindVariable[i], column, bindColumn.first))
                    } catch (e: IndexOutOfBoundsException) {
                        log.debug("${bindVariable[i]} : Unknown column")
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

    private fun isColumnExist(tableName: String, columnName: String, tablesMeta: Map<String, Any>): Boolean {
        if (columnName == "*") {
            return true
        }

        val tableMeta = tablesMeta[tableName.toLowerCase()] as Map<String, Any>?
        return tableMeta?.let { table ->
            val columns = table["columns"] as List<Map<String, String>>
            columns.any { it["name"] == columnName.toLowerCase() }
        } ?: false
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

    fun emit(event: ParseEvent) {
        listeners.forEach { it.onEvent(event) }
    }

    fun emit(eventType: ParseEvent.Type) {
        listeners.forEach { it.onEvent(ParseEvent(eventType, mapOf())) }
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
        parseEventEmitter.emit(ParseEvent.Type.NEW_CONTEXT)

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
        parseEventEmitter.emit(ParseEvent.Type.END_CONTEXT)
    }

    override fun visit(setOpList: SetOperationList?) {
        parseEventEmitter.emit(
            ParseEvent(
                ParseEvent.Type.TRACE,
                mapOf("trace" to "visit: setOpList: SetOperationList?")
            )
        )
        setOpList?.let {
            it.selects.forEach { selectBody ->
                selectBody.accept(this)
            }
        }
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
                    parseEventEmitter.emit(ParseEvent.Type.NEW_BIND)
                }

                expressionList.expressions?.let { expressions ->
                    expressions.forEach {
                        it.accept(this)
                    }
                }

                if (hasBindVariable) {
                    parseEventEmitter.emit(ParseEvent.Type.END_BIND)
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
                parseEventEmitter.emit(ParseEvent.Type.NEW_BIND)
            }

            it.leftExpression.accept(this)
            it.rightExpression.accept(this)

            if (it.leftExpression is JdbcNamedParameter || it.rightExpression is JdbcNamedParameter) {
                parseEventEmitter.emit(ParseEvent.Type.END_BIND)
            }
        }
    }

    override fun visit(expr: InExpression?) {
        expr?.let {
            if (it.leftExpression is JdbcNamedParameter) {
                parseEventEmitter.emit(ParseEvent.Type.NEW_BIND)
            }

            it.leftExpression.accept(this)

            if (it.leftExpression is JdbcNamedParameter) {
                parseEventEmitter.emit(ParseEvent.Type.END_BIND)
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