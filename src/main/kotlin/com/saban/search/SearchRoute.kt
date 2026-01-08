package com.saban.search

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

fun Route.searchRoute() {
    val logger = LoggerFactory.getLogger("SearchRoute")
    val searchService: SearchService by inject()
    /**
     * Find matches across all languages
     */
    get("/search/{searchText}") {
        val searchText = call.parameters["searchText"]

        if (searchText.isNullOrBlank()) {
            return@get call.respond(emptyMap<String, List<SearchResult>>())
        }

        try {
            val matches = searchService.search(searchText)
            call.respond(matches)
        } catch (e: Exception) {
            logger.error("Failed to find matches.", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}