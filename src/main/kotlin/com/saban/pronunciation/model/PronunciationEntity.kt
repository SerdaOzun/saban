package com.saban.pronunciation.model

import com.saban.pronunciation.PronunciationRepository
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.OffsetDateTime

@Serializable
data class PronunciationEntity(
    val id: Int,
    val text: String,
    val languageId: Int,
    val userId: Int,
    val publicUrl: String,
    @Contextual val createdAt: OffsetDateTime,
    val isApproved: Boolean
) {
    constructor(resultRow: ResultRow) : this(
        resultRow[PronunciationRepository.PronunciationTable.id].value,
        resultRow[PronunciationRepository.PronunciationTable.text],
        resultRow[PronunciationRepository.PronunciationTable.languageId].value,
        resultRow[PronunciationRepository.PronunciationTable.userId].value,
        resultRow[PronunciationRepository.PronunciationTable.s3Key],
        resultRow[PronunciationRepository.PronunciationTable.createdAt],
        resultRow[PronunciationRepository.PronunciationTable.isApproved]
    )
}