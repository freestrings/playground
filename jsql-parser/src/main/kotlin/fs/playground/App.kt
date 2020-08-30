package fs.playground

import net.sf.jsqlparser.expression.*
import net.sf.jsqlparser.expression.Function
import net.sf.jsqlparser.expression.operators.relational.*
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.schema.Table
import net.sf.jsqlparser.statement.select.*

fun main(args: Array<String>) {
    val sql = getSql9()
    println("input: $sql")
    val select = CCJSqlParserUtil.parse(sql) as Select
    select.selectBody.accept(SimpleSelectVisitor())

}

class SimpleFromItemVisitor : FromItemVisitorAdapter() {
    override fun visit(table: Table?) {
        println("table: ${table?.let { it.name }} - ${table?.let { it.alias }}")
        super.visit(table)
    }

    override fun visit(subSelect: SubSelect?) {
        println("subselect")
        subSelect?.let { it.selectBody.accept(SimpleSelectVisitor()) }
    }

    override fun visit(subjoin: SubJoin?) {
        println("subjoin")
        super.visit(subjoin)
    }

    override fun visit(lateralSubSelect: LateralSubSelect?) {
        println("lateral subselect")
        super.visit(lateralSubSelect)
    }

    override fun visit(valuesList: ValuesList?) {
        println("value list")
        super.visit(valuesList)
    }
}

class SimpleSelectVisitor : SelectVisitorAdapter() {
    override fun visit(plainSelect: PlainSelect?) {
        println("plain select")
        plainSelect?.let { plainSelect ->
            plainSelect.selectItems.forEach { it.accept(SimpleSelectItemVisitor()) }
            plainSelect.fromItem.accept(SimpleFromItemVisitor())
            plainSelect.where?.let { it.accept(SimpleExpressionVisitor()) }
            plainSelect.joins?.let {
                it.forEach { join ->
                    join.rightItem.accept(SimpleFromItemVisitor())
                }
            }
        }
    }

    override fun visit(setOpList: SetOperationList?) {
        println("setop list")
        setOpList?.let { it.plainSelects.forEach(this::visit) }
    }

    override fun visit(withItem: WithItem?) {
        println("not implementation")
    }
}

class SimpleSelectItemVisitor : SelectItemVisitorAdapter() {

    override fun visit(columns: AllTableColumns?) {
        columns?.let {
            val table = it.table
            println("table: ${table?.let { it.name }} - ${table?.let { it.alias }}")
        }
    }

    override fun visit(item: SelectExpressionItem?) {
        item?.let {
            it.expression.accept(SimpleExpressionVisitor())
        }
    }
}

class SimpleExpressionVisitor : ExpressionVisitorAdapter() {

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

    override fun visit(parameter: JdbcParameter?) {
        super.visit(parameter)
    }

    override fun visit(parameter: JdbcNamedParameter?) {
        super.visit(parameter)
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
        subSelect?.let { it.accept(SimpleFromItemVisitor()) }
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
        expr?.let { it.subSelect.accept(SimpleFromItemVisitor()) }
    }

    override fun visit(expr: AnyComparisonExpression?) {
        expr?.let { it.subSelect.accept(SimpleFromItemVisitor()) }
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
                tab1 t1,
                tab2 t2
            where t1.a = t2.a
                and t1.b = t2.b(+)
        )
    """.trimIndent()
}

fun getSql2(): String? {
    return """
        select * from tab1 t1
            inner join tab2 t2 on t1.a = t2.a
    """.trimIndent()
}

fun getSql2_1(): String? {
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