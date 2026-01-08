package com.saban.pronunciation.model

import com.saban.languages.LanguageRepository
import com.saban.pronunciation.PronunciationRepository
import org.jetbrains.exposed.v1.core.ResultRow

data class Word(
    val wordId: Int,
    val word: String,
    val languageId: Int,
    val language: String
) {
    constructor(resultRow: ResultRow) : this(
        wordId = resultRow[PronunciationRepository.PronunciationTable.id].value,
        word = resultRow[PronunciationRepository.PronunciationTable.text],
        languageId = resultRow[LanguageRepository.LanguageTable.id].value,
        language = resultRow[LanguageRepository.LanguageTable.languageName],
    )
}