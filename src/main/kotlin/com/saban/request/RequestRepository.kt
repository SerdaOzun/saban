package com.saban.request

import com.saban.languages.LanguageRepository
import com.saban.user.UserRepository
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.OffsetDateTime
import java.time.ZoneOffset

class RequestRepository {
    object RequestTable : IntIdTable("request") {
        val text = text("phrase_text")
        val languageId = reference("language_id", LanguageRepository.LanguageTable)
        val requestedBy = reference("requested_by", UserRepository.UserEntity)
        val createdAt = timestampWithTimeZone(name = "created_at")
        val done = bool("done").default(false)
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
            it[this.done] = false
        }.value
    }

    fun getRequests(language: String? = null, offset: Int, count: Int): PaginatedPronunciationResponse = transaction {
        val query = joinedRequestTable.select(
            RequestTable.id,
            RequestTable.createdAt,
            LanguageRepository.LanguageTable.id,
            LanguageRepository.LanguageTable.languageName,
            RequestTable.text,
            UserRepository.UserEntity.username
        ).where {
            RequestTable.done eq false
        }

        language?.let {
            query.andWhere { LanguageRepository.LanguageTable.languageName eq language }
        }

        val totalCount = query.count()
        val data = query
            .offset(offset.toLong())
            .limit(count)
            .orderBy(RequestTable.createdAt, SortOrder.DESC)
            .map(::RequestEntity)

        PaginatedPronunciationResponse(totalCount, data)
    }

    fun read(id: Int): RequestEntity? = transaction {
        joinedRequestTable.selectAll().where { RequestTable.id eq id }.singleOrNull()?.let(::RequestEntity)
    }

    fun updateRequestDone(requestId: Int) = transaction {
        RequestTable.update(where = { RequestTable.id eq requestId }) {
            it[RequestTable.done] = true
        }
    }

}