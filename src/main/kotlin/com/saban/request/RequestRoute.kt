package com.saban.request

import com.saban.plugins.UserSession
import com.saban.plugins.getUser
import com.saban.plugins.getUserSession
import com.saban.pronunciation.PronunciationService
import com.saban.pronunciation.model.PaginatedPronunciationsRequest
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

fun Route.requestRoute() {
    val logger = LoggerFactory.getLogger("RequestRoute")
    val requestService: RequestService by inject()
    val pronunciationService: PronunciationService by inject()

    route("/request") {
        authenticate("auth-session") {
            post("/save") {
                try {
                    val request = call.receive<PronunciationSaveRequest>()
                    val userId = call.getUserSession()?.userId ?: return@post call.respond(HttpStatusCode.Unauthorized)
                    requestService.save(userId, request)
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    logger.error("Failed to save request", e)
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }

            post("/upload/{requestId}") {
                val user = call.principal<UserSession>()?.getUser()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val requestId = call.parameters["requestId"]?.toIntOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "RequestId missing")

                val request = requestService.get(requestId)
                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Request not found")

                val multipart = call.receiveMultipart()
                try {
                    val (fileName, audioFile) = pronunciationService.extractFileAndFilename(
                        multipart,
                        request.text,
                        user.username
                    )
                    pronunciationService.savePronunciation(audioFile, fileName, request.text, user.id, request.language)
                    requestService.updateRequestDone(requestId)
                    call.respond(HttpStatusCode.OK, "File uploaded successfully")
                } catch (e: Exception) {
                    logger.error("Failed to save pronunciation for request: ${e.message}", e)
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }

        post("/paginated") {
            try {
                val request = call.receive<PaginatedPronunciationsRequest>()
                val response = requestService.get(request)
                call.respond(response)
            } catch (e: Exception) {
                logger.error("Failed to fetch pronunciation requests", e)
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}