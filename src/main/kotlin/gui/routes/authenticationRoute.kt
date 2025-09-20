package com.saban.gui.routes

import com.saban.user.model.RegistrationRequest
import com.saban.user.repository.UserRepository
import com.saban.plugins.UserSession
import com.saban.user.service.AuthenticationService
import com.saban.util.SabanResult
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.koin.ktor.ext.inject

fun Routing.handleAuthenticationRoute() {
    val authenticationService: AuthenticationService by inject()
    val userRepository by inject<UserRepository>()

    route("/user") {
        post("/register") {
            val registrationRequest = call.receive<RegistrationRequest>()
            val result = authenticationService.registerUser(registrationRequest)

            when (result) {
                is SabanResult.ErrorResult -> call.respond(HttpStatusCode.BadRequest, result.errorMessage)
                is SabanResult.SuccessResult -> call.respond(HttpStatusCode.OK, result.successMessage)
            }
        }

        post("/logout") {
            call.sessions.clear<UserSession>()
            call.respond(HttpStatusCode.OK, "Logged out")
        }

        authenticate("auth-form") {
            post("/login") {
                val principal = call.principal<UserIdPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")

                val userModel = userRepository.findUserByEmail(principal.name)
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")

                val user = UserSession(
                    email = userModel.email,
                    username = userModel.username,
                    userId = userModel.id
                )

                call.sessions.set(user)

                call.respond(HttpStatusCode.OK, user)
            }
        }

        authenticate("auth-session") {
            get("/login-check") {
                val session = call.sessions.get<UserSession>()

                if (session != null) {
                    call.respond(HttpStatusCode.OK, session)
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "No active session")
                }
            }
        }

    }
}
