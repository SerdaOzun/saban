package com.saban.core.service

import com.saban.core.model.Language
import com.saban.core.model.Word
import com.saban.core.repository.LanguageRepository
import com.saban.core.repository.PronunciationRepository
import com.saban.core.repository.WordRepository
import com.saban.gui.model.PronunciationResult
import com.saban.gui.model.SearchResult
import com.saban.gui.model.requests.SearchRequest
import com.saban.util.LangNotFoundException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PronunciationService : KoinComponent {
    private val pronunciationRepo by inject<PronunciationRepository>()
    private val wordRepo by inject<WordRepository>()
    private val langRepo by inject<LanguageRepository>()

    /**
     * Get or save a new word
     */
    suspend fun getOrSaveWord(word: String, language: String): Word {
        return findWord(word, language) ?: run {
            val langId = langRepo.findLanguage(language)?.id
                ?: throw LangNotFoundException("Language $language not found")

            val wordId = wordRepo.saveWord(word, langId)

            Word(
                wordId = wordId,
                word = word,
                languageId = langId,
                language = language
            )
        }
    }

    suspend fun getPronunciations(word: String, language: String): List<PronunciationResult> =
        pronunciationRepo.getPronunciations(word, language)

    fun findMatches(searchText: String): Map<String, List<SearchResult>> =
        wordRepo.findMatches(searchText)

    suspend fun findWord(word: String, language: String): Word? =
        wordRepo.findWord(word, language)

    suspend fun findLanguage(language: String): Language? =
        langRepo.findLanguage(language)

    fun getLanguages(): List<Language> = langRepo.getLanguages()

    /**
     * @return pronunciation id
     */
    suspend fun savePronunciation(userId: Int, wordId: Int, fileKey: String): Int =
        pronunciationRepo.savePronunciation(userId, wordId, fileKey)
}
