package com.saban.pronunciation.model

import com.saban.pronunciation.PronunciationRepository
import com.saban.user.UserRepository
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.OffsetDateTime

@Serializable
data class PronunciationResult(
    val username: String,
    val word: String,
    val s3key: String,
    val url: String = "",
    @Contextual val createdAt: OffsetDateTime
) {
    constructor(row: ResultRow) : this(
        username = row[UserRepository.UserEntity.username],
        word = row[PronunciationRepository.PronunciationTable.text],
        s3key = row[PronunciationRepository.PronunciationTable.s3Key],
        createdAt = row[PronunciationRepository.PronunciationTable.createdAt]
    )
}