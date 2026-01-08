package com.saban.request

import com.saban.languages.LanguageRepository
import com.saban.user.UserRepository
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.OffsetDateTime

@Serializable
data class RequestEntity(
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