package com.saban.core.model.core.model

import com.saban.core.repository.PronunciationRepository
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.OffsetDateTime

@Serializable
data class Request(
    val id: Int,
    val text: String,
    val languageId: Int,
    val userId: Int,
    @Contextual val createdAt: OffsetDateTime
) {
    constructor(resultRow: ResultRow) : this(
        resultRow[PronunciationRepository.PronunciationTable.id].value,
        resultRow[PronunciationRepository.PronunciationTable.text],
        resultRow[PronunciationRepository.PronunciationTable.languageId].value,
        resultRow[PronunciationRepository.PronunciationTable.userId].value,
        resultRow[PronunciationRepository.PronunciationTable.createdAt]
    )
}
