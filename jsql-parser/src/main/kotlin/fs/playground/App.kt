package fs.playground

import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.statement.select.Select
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    val log = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
    val tableMeta = ResourceLoader.loadMeta("table.json")
    val bindMeta = ResourceLoader.loadMeta("bind.json")
    val sql = getSql16()
    log.debug(sql)
    val bounds = calcBinds(sql, tableMeta)
    val bindValueProvider = BindValueProvider(bindMeta)
    bounds.forEach { bindMeta ->
        log.debug("$bindMeta")
        val bound = bindValueProvider.get(tableName = bindMeta.tableName, columnName = bindMeta.columnName, data = null)
        log.info("$bound")
    }
}

fun calcBinds(sql: String?, tableMeta: Map<String, Any>): List<BindMeta> {
    val log = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
    val selectCandidate = CCJSqlParserUtil.parse(sql)
    if (selectCandidate is Select) {
        val parseEventEmitter = ParseEventEmitter()
        val errors = mutableListOf<ErrorMeta>()
        val binds = mutableListOf<BindMeta>()
        parseEventEmitter.addLister(DefaultParseEventListener(tableMeta, errors, binds))
        selectCandidate.selectBody.accept(SimpleSelectVisitor(parseEventEmitter))

        if (errors.size > 0) {
            throw InvalidSqlException("Bad SQL Grammar", errors)
        }

        return binds
    } else {
        log.warn("Not a select query: $sql")
        return emptyList()
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