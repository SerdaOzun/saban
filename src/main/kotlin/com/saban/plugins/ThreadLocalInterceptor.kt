package com.saban.plugins

import io.ktor.server.application.*
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.withContext

private val threadLocal = ThreadLocal<UserSession?>()

fun getUserSession(): UserSession? = threadLocal.get()

fun Application.configureThreadLocalInterceptor() {
    intercept(ApplicationCallPipeline.Plugins) {
        withContext(coroutineContext + threadLocal.asContextElement(call.getUserSession())) {
            proceed()
        }
    }
}