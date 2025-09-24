package com.saban.core.repository


import org.jetbrains.exposed.v1.core.ColumnType
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.QueryBuilder
import org.jetbrains.exposed.v1.core.stringLiteral
import org.postgresql.util.PGobject

/**
 * Credit: https://codeodessey.substack.com/p/full-text-search-and-relevance-ranking
 */

private const val TS_VECTOR_SQL_TYPE = "tsvector"

class TsVectorColumnType : ColumnType<String>(nullable = true) {
    override fun sqlType(): String = TS_VECTOR_SQL_TYPE

    override fun valueFromDB(value: Any): String? {
        return value as String
    }

    override fun valueToDB(value: String?): Any? {
        return PGobject().apply {
            type = TS_VECTOR_SQL_TYPE
            this.value = value
        }.value
    }


    override fun notNullValueToDB(value: String): Any {
        return PGobject().apply {
            type = TS_VECTOR_SQL_TYPE
            this.value = value
        }
    }

    override fun nonNullValueToString(value: String): String {
        return "'$value'"
    }
}

class TsQueryOp(val tsVectorColumn: Expression<String>, val query: String) : Op<Boolean>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        queryBuilder.append(tsVectorColumn)
        queryBuilder.append(" @@ to_tsquery('simple_unaccent', ")
        queryBuilder.append(stringLiteral(sanitizeQuery(query))) // Ensures proper escaping of the query string
        queryBuilder.append(")")
    }
}

fun sanitizeQuery(query: String): String {
    // Replace spaces with '&' for logical AND (to match all terms)
    return query.trim().replace(Regex("\\s+"), " & ")
}
