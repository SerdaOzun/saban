package com.saban.gui

import com.saban.plugins.getUserSession
import com.saban.util.countries
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

fun Route.settingsRoute() {
    val logger = LoggerFactory.getLogger("SettingsRoute")
    val settingsService: SettingsService by inject()

    authenticate("auth-session") {
        route("/settings") {
            post("/country/{country}") {
                val userId = call.getUserSession()?.userId ?: return@post call.respond(HttpStatusCode.Forbidden)

                val country = call.parameters["country"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "country missing")

                if (country !in countries) {
                    return@post call.respond(HttpStatusCode.BadRequest, "Country not allowed")
                }

                try {
                    settingsService.updateCountry(userId, country)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    logger.error("Failed to update country", e)
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }

            get("/data") {
                val userId = call.getUserSession()?.userId ?: return@get call.respond(HttpStatusCode.Forbidden)

                try {
                    val settings = settingsService.getSettings(userId)
                    call.respond(settings)
                } catch (e: Exception) {
                    logger.error("Failed to fetch settings data", e)
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}