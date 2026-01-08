package com.saban.gui

import io.ktor.server.http.content.*
import io.ktor.server.routing.*

fun Routing.handleHtmlRoute() {
    staticResources(remotePath = "/", "/gui") {
        default("index.html")
    }
}
