package com.saban.plugins.plugins

import com.saban.user.SessionRepository
import io.ktor.server.sessions.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DbSessionStorage : SessionStorage, KoinComponent {
    private val sessionRepository: SessionRepository by inject()

    override suspend fun write(id: String, value: String) {
        sessionRepository.write(id, value)
    }

    override suspend fun invalidate(id: String) {
        sessionRepository.invalidate(id)
    }

    override suspend fun read(id: String): String =
        sessionRepository.read(id) ?: throw NoSuchElementException("Session $id not found")
}