package com.saban.core.repository

import com.saban.core.model.Pronunciation
import com.saban.core.repository.WordRepository.WordEntity
import com.saban.gui.model.PronunciationResult
import com.saban.user.repository.UserRepository
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.OffsetDateTime
import java.time.ZoneOffset

class PronunciationRepository {
    object PronunciationTable : IntIdTable("pronunciation") {
        val userId = reference("user_id", UserRepository.UserEntity)
        val wordId = reference("word_id", WordRepository.WordEntity)
        val s3Key = text("s3_key") //sound file
        val createdAt = timestampWithTimeZone(name = "created_at")

        //When users collect bad ratings, their pronunciations must be approved first
        val isApproved = bool("is_approved")
    }

    /**
     * Join Pronunciations on Word, User and Language
     */
    private val joinedPronunciationsTable = PronunciationTable.join(
        otherTable = WordEntity,
        joinType = JoinType.INNER,
        onColumn = PronunciationTable.wordId,
        otherColumn = WordEntity.id
    ).join(
        otherTable = UserRepository.UserEntity,
        joinType = JoinType.INNER,
        onColumn = PronunciationTable.userId,
        otherColumn = UserRepository.UserEntity.id
    ).join(
        otherTable = LanguageRepository.LanguageTable,
        joinType = JoinType.INNER,
        onColumn = WordEntity.language,
        otherColumn = LanguageRepository.LanguageTable.id
    )

    suspend fun savePronunciation(
        userId: Int,
        wordId: Int,
        fileKey: String,
    ): Int = newSuspendedTransaction {
        PronunciationTable.insertAndGetId {
            it[this.userId] = userId
            it[this.wordId] = wordId
            it[this.s3Key] = fileKey
            it[this.createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
            it[this.isApproved] = true
        }.value
    }

    suspend fun getPronunciations(word: String, language: String): List<PronunciationResult> = newSuspendedTransaction {
        joinedPronunciationsTable.select(
            PronunciationTable.s3Key,
            PronunciationTable.createdAt,
            WordEntity.text,
            UserRepository.UserEntity.username
        ).where { WordEntity.text like "${word.trim()}%" }
            .andWhere { LanguageRepository.LanguageTable.languageName eq language }
            .map(::PronunciationResult)
    }

    suspend fun findPronunciation(wordId: Int): Pronunciation? = newSuspendedTransaction {
        PronunciationTable.selectAll().where { PronunciationTable.wordId eq wordId }.singleOrNull()?.let {
            Pronunciation(it)
        }
    }
}