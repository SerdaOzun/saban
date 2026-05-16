package com.saban

import com.saban.gui.handleHtmlRoute
import com.saban.gui.settingsRoute
import com.saban.languages.Language
import com.saban.languages.LanguageRepository.LanguageTable
import com.saban.plugins.*
import com.saban.pronunciation.pronunciationRoute
import com.saban.request.requestRoute
import com.saban.search.searchRoute
import com.saban.user.handleAuthenticationRoute
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

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
    listOf(
        Language(-1, "german", "de", false),
        Language(-1, "french", "fr", false),
        Language(-1, "arabic", "ar", true),
    ).forEach { lang ->
        LanguageTable.insertIgnore {
            it[languageName] = lang.name
            it[languageCode] = lang.code
            it[rtl] = lang.rtl
        }
    }
}
