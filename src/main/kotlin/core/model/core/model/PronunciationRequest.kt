package com.saban.core.model.core.model

import com.saban.core.repository.LanguageRepository
import com.saban.core.repository.RequestRepository
import com.saban.user.repository.UserRepository
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.OffsetDateTime

@Serializable
data class PronunciationRequest(
    val id: Int,
    val text: String,
    val language: String,
    val languageId: Int,
    val requestedBy: String,
    @Contextual val createdAt: OffsetDateTime
) {
    constructor(resultRow: ResultRow) : this(
        resultRow[RequestRepository.RequestTable.id].value,
        resultRow[RequestRepository.RequestTable.text],
        resultRow[LanguageRepository.LanguageTable.languageName],
        resultRow[LanguageRepository.LanguageTable.id].value,
        resultRow[UserRepository.UserEntity.username],
        resultRow[RequestRepository.RequestTable.createdAt]
    )
}
