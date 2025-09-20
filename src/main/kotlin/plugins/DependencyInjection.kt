package com.saban.plugins

import com.saban.core.repository.LanguageRepository
import com.saban.core.repository.PronunciationRepository
import com.saban.core.repository.VotingRepository
import com.saban.core.repository.WordRepository
import com.saban.core.service.PronunciationService
import com.saban.user.service.AuthenticationService
import com.saban.gui.service.GuiService
import com.saban.storage.S3Service
import com.saban.user.repository.UserRepository
import io.ktor.server.application.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDependencyInjection() {
    install(Koin) {
        slf4jLogger()
        modules(module {
            single(createdAtStart = true) { SabanConfig() }
            single { LanguageRepository() }
            single { WordRepository() }
            single { PronunciationRepository() }
            single { UserRepository() }
            single { VotingRepository() }
            single { AuthenticationService(get()) }
            single { GuiService() }
            single { S3Service(get()) }
            single { PronunciationService() }
        })
    }
}

inline fun <reified T : Any> getKoinInstance(): T {
    return object : KoinComponent {
        val value: T by inject()
    }.value
}