package com.saban.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCors() {
    install(CORS) {
        anyHost()
        anyMethod()

        allowCredentials = true

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.Cookie)

        exposeHeader("User_session")
        exposeHeader(HttpHeaders.Location)
        exposeHeader(HttpHeaders.SetCookie)
    }
}