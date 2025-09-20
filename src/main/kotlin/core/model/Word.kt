package com.saban.core.model

import com.saban.core.repository.LanguageRepository
import com.saban.core.repository.WordRepository
import org.jetbrains.exposed.sql.ResultRow

data class Word(
    val wordId: Int,
    val word: String,
    val languageId: Int,
    val language: String
) {
    constructor(resultRow: ResultRow) : this(
        wordId = resultRow[WordRepository.WordEntity.id].value,
        word = resultRow[WordRepository.WordEntity.text],
        languageId = resultRow[LanguageRepository.LanguageTable.id].value,
        language = resultRow[LanguageRepository.LanguageTable.languageName],
    )
}