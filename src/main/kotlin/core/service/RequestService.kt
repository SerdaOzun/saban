package com.saban.core.service

import com.saban.core.repository.LanguageRepository
import com.saban.core.repository.RequestRepository
import com.saban.gui.model.requests.PronunciationRequest
import com.saban.util.MissingLanguageException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RequestService : KoinComponent {
    private val requestRepo by inject<RequestRepository>()
    private val langRepo by inject<LanguageRepository>()

    fun getRequests(language: String) = requestRepo.getRequests(language)
    fun getRequest(id: Int) = requestRepo.read(id)
    fun save(userId: Int, request: PronunciationRequest) {
        val langId = langRepo.read(request.language)?.id
            ?: throw MissingLanguageException("Language '${request.language}' not found")
        requestRepo.saveRequest(userId, request.text, langId)
    }
}