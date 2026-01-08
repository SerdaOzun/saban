package com.saban.search

import com.saban.pronunciation.PronunciationRepository
import org.koin.core.component.KoinComponent

class SearchService(
    private val pronunciationRepository: PronunciationRepository
): KoinComponent {

    /**
     * Find matches across all languages for the search term
     */
    fun search(searchText: String): Map<String, List<SearchResult>> =
        pronunciationRepository.searchEntriesByLanguage(searchText)
}