package com.saban.gui.model

import com.saban.core.repository.PronunciationRepository
import com.saban.user.repository.UserRepository
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
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
