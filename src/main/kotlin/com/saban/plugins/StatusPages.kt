package com.saban.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*

fun Application.configureStatusPages() {
    install(StatusPages) {
//        status(HttpStatusCode.Unauthorized) {
//            call.respond("/login", HttpStatusCode.Unauthorized)
//        }
    }
}
