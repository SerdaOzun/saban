package com.saban.plugins

import com.saban.gui.SettingsService
import com.saban.languages.LanguageRepository
import com.saban.languages.LanguageService
import com.saban.pronunciation.PronunciationRepository
import com.saban.pronunciation.PronunciationService
import com.saban.request.RequestRepository
import com.saban.request.RequestService
import com.saban.search.SearchService
import com.saban.s3.S3Service
import com.saban.user.AuthenticationService
import com.saban.user.SessionRepository
import com.saban.user.UserRepository
import com.saban.voting.VotingRepository
import io.ktor.server.application.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.onOptions
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDependencyInjection() {
    install(Koin) {
        slf4jLogger()
        modules(module {
            singleOf(::SabanConfig).onOptions { createdAtStart() }
            singleOf(::SessionRepository)
            singleOf(::LanguageRepository)
            singleOf(::PronunciationRepository)
            singleOf(::UserRepository)
            singleOf(::VotingRepository)
            singleOf(::AuthenticationService)
            singleOf(::S3Service)
            singleOf(::PronunciationService)
            singleOf(::RequestRepository)
            singleOf(::RequestService)
            singleOf(::LanguageService)
            singleOf(::SearchService)
            singleOf(::SettingsService)
        })
    }
}

inline fun <reified T : Any> getKoinInstance(): T {
    return object : KoinComponent {
        val value: T by inject()
    }.value
}