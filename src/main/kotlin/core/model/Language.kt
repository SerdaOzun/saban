package com.saban.core.model

import com.saban.core.repository.LanguageRepository
import org.jetbrains.exposed.v1.core.ResultRow

data class Language(
    val id: Int,
    val name: String,
    val code: String,
    val rtl: Boolean
) {
    constructor(resultRow: ResultRow) : this(
        id = resultRow[LanguageRepository.LanguageTable.id].value,
        name = resultRow[LanguageRepository.LanguageTable.languageName],
        code = resultRow[LanguageRepository.LanguageTable.languageCode],
        rtl = resultRow[LanguageRepository.LanguageTable.rtl]
    )
}