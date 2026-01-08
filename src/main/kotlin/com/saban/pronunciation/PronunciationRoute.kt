package com.saban.pronunciation

import com.saban.plugins.UserSession
import com.saban.plugins.getUser
import com.saban.pronunciation.model.PronunciationSearchRequest
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

fun Route.pronunciationRoute() {
    val logger = LoggerFactory.getLogger("PronunciationRoute")
    val pronunciationService by inject<PronunciationService>()

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
                val (fileName, audioFile) = pronunciationService.extractFileAndFilename(multipart, word, user.username)
                pronunciationService.savePronunciation(audioFile, fileName, word, user.id, language)
                call.respond(HttpStatusCode.OK, "File uploaded successfully")
            } catch (e: Exception) {
                logger.error("Failed to save pronunciation: ${e.message}", e)
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }

    /**
     * Find pronunciations for a specific search term and language
     */
    post("/getPronunciations") {
        val request = call.receive<PronunciationSearchRequest>()

        try {
            val pronunciations = pronunciationService.getPronunciations(request.text, request.language)
            call.respond(pronunciations)
        } catch (e: Exception) {
            logger.error("Failed to find pronunciations", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    authenticate("auth-session") {
        post("/request/upload/{requestId}") {
            val user = call.principal<UserSession>()?.getUser()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val requestId = call.parameters["requestId"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, "RequestId missing")

            val request = pronunciationService.getRequest(requestId)
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Request not found")

            val multipart = call.receiveMultipart()
            try {
                val (fileName, audioFile) = pronunciationService.extractFileAndFilename(
                    multipart,
                    request.text,
                    user.username
                )
                pronunciationService.savePronunciation(audioFile, fileName, request.text, user.id, request.language)
                call.respond(HttpStatusCode.OK, "File uploaded successfully")
            } catch (e: Exception) {
                logger.error("Failed to save pronunciation for request: ${e.message}", e)
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}