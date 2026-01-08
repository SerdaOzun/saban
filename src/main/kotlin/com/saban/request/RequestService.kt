package com.saban.request

import com.saban.languages.LanguageService
import com.saban.pronunciation.model.PaginatedPronunciationsRequest
import com.saban.util.MissingLanguageException
import org.koin.core.component.KoinComponent

class RequestService(
    private val requestRepo: RequestRepository,
    private val languageService: LanguageService
) : KoinComponent {

    fun get(request: PaginatedPronunciationsRequest): PaginatedPronunciationResponse {
        return requestRepo.getRequests(request.language, request.offset, request.limit)
    }

    fun get(id: Int) = requestRepo.read(id)
    fun save(userId: Int, request: PronunciationSaveRequest) {
        val langId = languageService.findLanguage(request.language)?.id
            ?: throw MissingLanguageException("Language '${request.language}' not found")
        requestRepo.saveRequest(userId, request.text, langId)
    }
}