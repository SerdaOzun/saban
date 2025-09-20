package com.saban.core.model

import com.saban.core.repository.PronunciationRepository
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import java.time.OffsetDateTime

@Serializable
data class Pronunciation(
    val userId: Int,
    val publicUrl: String,
    val wordId: Int,
    @Contextual val createdAt: OffsetDateTime,
    val isApproved: Boolean
) {
    constructor(resultRow: ResultRow) : this(
        resultRow[PronunciationRepository.PronunciationTable.userId].value,
        resultRow[PronunciationRepository.PronunciationTable.s3Key],
        resultRow[PronunciationRepository.PronunciationTable.wordId].value,
        resultRow[PronunciationRepository.PronunciationTable.createdAt],
        resultRow[PronunciationRepository.PronunciationTable.isApproved]
    )
}