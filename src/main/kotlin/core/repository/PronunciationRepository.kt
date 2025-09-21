package com.saban.core.repository

import com.saban.core.model.Pronunciation
import com.saban.core.model.Word
import com.saban.gui.model.PronunciationResult
import com.saban.gui.model.SearchResult
import com.saban.user.repository.UserRepository
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.OffsetDateTime
import java.time.ZoneOffset

class PronunciationRepository {
    object PronunciationTable : IntIdTable("pronunciation") {
        val text = text("phrase_text")
        val searchTsv = registerColumn<String>("search_tsv", TsVectorColumnType()).databaseGenerated()
        val languageId = reference("language_id", LanguageRepository.LanguageTable)
        val userId = reference("user_id", UserRepository.UserEntity)
        val s3Key = text("s3_key") //sound file
        val createdAt = timestampWithTimeZone(name = "created_at")

        //When users collect bad ratings, their pronunciations must be approved first
        val isApproved = bool("is_approved")
    }

    /**
     * Join Pronunciations on Word, User and Language
     */
    private val joinedPronunciationsTable = PronunciationTable.join(
        otherTable = UserRepository.UserEntity,
        joinType = JoinType.INNER,
        onColumn = PronunciationTable.userId,
        otherColumn = UserRepository.UserEntity.id
    ).join(
        otherTable = LanguageRepository.LanguageTable,
        joinType = JoinType.INNER,
        onColumn = PronunciationTable.languageId,
        otherColumn = LanguageRepository.LanguageTable.id
    )

    fun savePronunciation(
        userId: Int,
        word: String,
        langId: Int,
        fileKey: String,
    ): Int = transaction {
        PronunciationTable.insertAndGetId {
            it[this.userId] = userId
            it[this.text] = word
            it[this.languageId] = langId
            it[this.s3Key] = fileKey
            it[this.createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
            it[this.isApproved] = true
        }.value
    }

    fun searchEntriesByLanguage(searchText: String): Map<String, List<SearchResult>> = transaction {
        val rankAlias = CustomTsRankFunction(PronunciationTable.searchTsv, searchText).alias("rank")

        PronunciationTable.innerJoin(
            LanguageRepository.LanguageTable,
            { PronunciationTable.languageId },
            { LanguageRepository.LanguageTable.id }
        ).select(PronunciationTable.id, PronunciationTable.text, LanguageRepository.LanguageTable.languageName)
            .where { TsQueryOp(PronunciationTable.searchTsv, searchText) }
            .groupBy(
                { it[LanguageRepository.LanguageTable.languageName] },
                { SearchResult(it[PronunciationTable.text], it[PronunciationTable.id].value) }
            )
    }

    fun getPronunciations(word: String, language: String): List<PronunciationResult> = transaction {
        joinedPronunciationsTable.select(
            PronunciationTable.s3Key,
            PronunciationTable.createdAt,
            PronunciationTable.text,
            UserRepository.UserEntity.username
        ).where { TsQueryOp(PronunciationTable.searchTsv, word) }
            .andWhere { LanguageRepository.LanguageTable.languageName eq language }
            .map(::PronunciationResult)
    }

    fun read(id: Int): Pronunciation? = transaction {
        PronunciationTable.selectAll().where { PronunciationTable.id eq id }.singleOrNull()?.let {
            Pronunciation(it)
        }
    }

    fun findWord(word: String, language: String): Word? = transaction {
        PronunciationTable.innerJoin(
            LanguageRepository.LanguageTable,
            { PronunciationTable.languageId },
            { LanguageRepository.LanguageTable.id }
        ).selectAll()
            .where { PronunciationTable.text.lowerCase() eq word.lowercase() and (LanguageRepository.LanguageTable.languageName eq language) }
            .singleOrNull()?.let { Word(it) }
    }
}