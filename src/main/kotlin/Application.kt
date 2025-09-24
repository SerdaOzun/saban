package com.saban

import com.saban.core.repository.LanguageRepository.LanguageTable
import com.saban.plugins.*
import com.saban.gui.routes.handleAuthenticationRoute
import com.saban.gui.routes.handleGuiRoute
import com.saban.gui.routes.handleHtmlRoute
import com.saban.storage.S3Service
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
        handleGuiRoute()
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
