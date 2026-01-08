package com.saban.request

import com.saban.plugins.getUserSession
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