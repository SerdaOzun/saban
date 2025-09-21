package com.saban.user.repository.user.repository

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent

class SessionRepository : KoinComponent {
    object SessionTable : Table("session_storage") {
        val id = text("id")
        val sessionValue = text("session_value")

        override val primaryKey = PrimaryKey(id)
    }

    fun write(id: String, value: String) = transaction {
        SessionTable.insert {
            it[SessionTable.id] = id
            it[SessionTable.sessionValue] = value
        }
    }

    fun read(id: String) = transaction {
        SessionTable.select(SessionTable.sessionValue).where { SessionTable.id eq id }.singleOrNull()
            ?.let { it[SessionTable.sessionValue] }
    }

    fun invalidate(id: String) = transaction {
        SessionTable.deleteWhere { SessionTable.id eq id }
    }
}