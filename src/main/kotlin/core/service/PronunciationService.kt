package com.saban.core.service

import com.saban.core.model.Language
import com.saban.core.repository.LanguageRepository
import com.saban.core.repository.PronunciationRepository
import com.saban.gui.model.PronunciationResult
import com.saban.gui.model.SearchResult
import com.saban.util.MissingLanguageException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PronunciationService : KoinComponent {
    private val pronunciationRepo by inject<PronunciationRepository>()
    private val langRepo by inject<LanguageRepository>()

    fun getPronunciations(word: String, language: String): List<PronunciationResult> =
        pronunciationRepo.getPronunciations(word, language)

    fun findMatches(searchText: String): Map<String, List<SearchResult>> =
        pronunciationRepo.searchEntriesByLanguage(searchText)

    fun findLanguage(language: String): Language? =
        langRepo.read(language)

    fun getLanguages(): List<Language> = langRepo.getLanguages()

    /**
     * @return pronunciation id
     */
    fun savePronunciation(userId: Int, word: String, lang: String, fileKey: String): Int {
        val langId= langRepo.read(lang)?.id ?: throw MissingLanguageException("Language '$lang' not found")
        return pronunciationRepo.savePronunciation(userId, word, langId, fileKey)
    }
}
