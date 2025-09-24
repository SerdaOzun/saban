package com.saban.gui.routes

import com.saban.gui.model.SearchResult
import com.saban.gui.model.requests.PronunciationRequest
import com.saban.plugins.UserSession
import com.saban.plugins.getUser
import com.saban.gui.service.GuiService
import com.saban.plugins.getUserSession
import com.saban.util.countries
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

fun Routing.handleGuiRoute() {
    val logger = LoggerFactory.getLogger("GUI-ROUTE-LOGGER")
    val guiService by inject<GuiService>()
    val generalErrorMessage = "An error occurred. Please try again later."
    route("/gui") {

        authenticate("auth-session") {
            post("/pronunciation/{lang}/{word}") {
                val user = call.principal<UserSession>()?.getUser()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val multipart = call.receiveMultipart()
                val language: String = call.parameters["lang"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Language must be specified.")
                val word: String = call.parameters["word"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Word must be specified.")

                try {
                    val (fileName, audioFile) = guiService.extractFileAndFilename(multipart, word, user.username)
                    guiService.savePronunciation(audioFile, fileName, word, user.id, language)
                    call.respond(HttpStatusCode.OK, "File uploaded successfully")
                } catch (e: Exception) {
                    logger.error("Failed to save pronunciation: ${e.message}", e)
                    call.respond(HttpStatusCode.InternalServerError, generalErrorMessage)
                }
            }
        }

        /**
         * Find matches across all languages
         */
        get("/search/{searchText}") {
            val searchText = call.parameters["searchText"]

            if (searchText.isNullOrBlank()) {
                return@get call.respond(emptyMap<String, List<SearchResult>>())
            }

            try {
                val matches = guiService.findMatches(searchText)
                call.respond(matches)
            } catch (e: Exception) {
                logger.error("Failed to find matches.", e)
                call.respond(HttpStatusCode.InternalServerError, generalErrorMessage)
            }
        }

        /**
         * Find pronunciations for a specific search term and language
         */
        post("/getPronunciations") {
            val request = call.receive<PronunciationRequest>()

            try {
                val pronunciations = guiService.getPronunciations(request)
                call.respond(pronunciations)
            } catch (e: Exception) {
                logger.error("Failed to find pronunciations", e)
                call.respond(HttpStatusCode.InternalServerError, generalErrorMessage)
            }
        }

        route("/settings") {
            post("/country/{country}") {
                val userId = call.getUserSession()?.userId ?: return@post call.respond(HttpStatusCode.Forbidden)

                val country = call.parameters["country"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "country missing")

                if (country !in countries) {
                    return@post call.respond(HttpStatusCode.BadRequest, "Country not allowed")
                }

                try {
                    guiService.updateCountry(userId, country)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    logger.error("Failed to update country", e)
                    call.respond(HttpStatusCode.InternalServerError, generalErrorMessage)
                }
            }

            get("/data") {
                val userId = call.getUserSession()?.userId ?: return@get call.respond(HttpStatusCode.Forbidden)

                try {
                    val settings = guiService.getSettings(userId)
                    call.respond(settings)
                } catch (e: Exception) {
                    logger.error("Failed to fetch settings data", e)
                    call.respond(HttpStatusCode.InternalServerError, generalErrorMessage)
                }
            }
        }

        post("/request") {
            val request = call.receive<PronunciationRequest>()

            try {
                //todo
                // Wort + Sprache in DB speichern

            } catch (e: Exception) {
                logger.error("Failed to save request", e)
                call.respond(HttpStatusCode.InternalServerError, generalErrorMessage)
            }
        }
    }
}