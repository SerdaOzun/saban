package com.saban.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureAdministration() {
    routing {
        route("/") {
//            install(RateLimiting) {
//                rateLimiter {
//                    type = TokenBucket::class
//                    capacity = 100
//                    rate = 10.seconds
//                }
//            }
        }
    }
}
