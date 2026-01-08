package com.saban

import com.saban.gui.handleHtmlRoute
import com.saban.gui.settingsRoute
import com.saban.languages.LanguageRepository.LanguageTable
import com.saban.plugins.*
import com.saban.pronunciation.pronunciationRoute
import com.saban.request.requestRoute
import com.saban.search.searchRoute
import com.saban.s3.S3Service
import com.saban.user.handleAuthenticationRoute
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.coroutines.launch
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.ktor.ext.inject

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureCors()
    configureSerialization()
    configureDependencyInjection()
    configureMigrations()
    configureDatabases()
    configureSecurity()
    configureThreadLocalInterceptor()
    configureHTTP()
    configureMonitoring()
    configureAdministration()
    configureStatusPages()
    installRoutes()

    insertTestData()
    val s3Service by inject<S3Service>()
    launch {
        s3Service.createLanguageBuckets()
    }
}

fun Application.installRoutes() {
    routing {
        handleHtmlRoute()
        handleAuthenticationRoute()
        route("/gui") {
            pronunciationRoute()
            requestRoute()
            searchRoute()
            settingsRoute()
        }
    }
}


fun insertTestData() = transaction {
    insertLanguages()
}

private fun insertLanguages() {
    LanguageTable.insertIgnore {
        it[languageName] = "german"
        it[languageCode] = "de"
        it[rtl] = false
    }
    LanguageTable.insertIgnore {
        it[languageName] = "french"
        it[languageCode] = "fr"
        it[rtl] = false
    }
    LanguageTable.insertIgnore {
        it[languageName] = "spanish"
        it[languageCode] = "esp"
        it[rtl] = false
    }
}
