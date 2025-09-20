package com.saban.gui.routes

import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import java.io.File

fun Routing.handleHtmlRoute() {
    staticResources(remotePath = "/", "gui") {
        default("index.html")
    }
}
