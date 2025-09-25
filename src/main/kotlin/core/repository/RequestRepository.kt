package com.saban.core.repository

import com.saban.core.model.core.model.Request
import com.saban.user.repository.UserRepository
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.OffsetDateTime
import java.time.ZoneOffset

class RequestRepository {
    object RequestTable : IntIdTable("request") {
        val text = text("phrase_text")
        val languageId = reference("language_id", LanguageRepository.LanguageTable)
        val requestedBy = reference("requested_by", UserRepository.UserEntity)
        val createdAt = timestampWithTimeZone(name = "created_at")
    }

    /**
     * Join Pronunciations on Word, User and Language
     */
    private val joinedRequestTable = RequestTable.join(
        otherTable = UserRepository.UserEntity,
        joinType = JoinType.INNER,
        onColumn = RequestTable.requestedBy,
        otherColumn = UserRepository.UserEntity.id
    ).join(
        otherTable = LanguageRepository.LanguageTable,
        joinType = JoinType.INNER,
        onColumn = RequestTable.languageId,
        otherColumn = LanguageRepository.LanguageTable.id
    )

    fun saveRequest(
        userId: Int,
        word: String,
        langId: Int
    ): Int = transaction {
        RequestTable.insertAndGetId {
            it[this.requestedBy] = userId
            it[this.text] = word
            it[this.languageId] = langId
            it[this.createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
        }.value
    }

    fun getRequests(language: String): List<Request> = transaction {
        joinedRequestTable.select(
            RequestTable.id,
            RequestTable.createdAt,
            LanguageRepository.LanguageTable.id,
            RequestTable.text,
            UserRepository.UserEntity.username
        ).where { LanguageRepository.LanguageTable.languageName eq language }
            .map(::Request)
    }

    fun read(id: Int): Request? = transaction {
        joinedRequestTable.selectAll().where { RequestTable.id eq id }.singleOrNull()?.let(::Request)
    }

}