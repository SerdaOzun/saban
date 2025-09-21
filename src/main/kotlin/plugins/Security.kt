package com.saban.plugins

import com.saban.plugins.plugins.DbSessionStorage
import com.saban.user.model.UserModel
import com.saban.user.repository.UserRepository
import com.saban.user.service.AuthenticationService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.io.File
import kotlin.time.Duration.Companion.days

fun Application.configureSecurity() {
    val authenticationService: AuthenticationService by inject()
    authenticationService.createAdminUser()

    install(Sessions) {
        val secretSignKey = hex("6819b57a326945c1968f45236589")
        cookie<UserSession>("user_session", DbSessionStorage()) {
            transform(SessionTransportTransformerMessageAuthentication(secretSignKey))
            cookie.path = "/"
            cookie.maxAgeInSeconds = 7.days.inWholeSeconds
            cookie.secure = false //todo aus config auslesen
            cookie.extensions["SameSite"] = "lax"
        }
    }

    install(Authentication) {
        form("auth-form") {
            userParamName = "email"
            passwordParamName = "password"

            validate { credentials ->
                authenticationService.login(credentials.name, credentials.password)?.let {
                    UserIdPrincipal(credentials.name)
                }
            }
            challenge {
                call.respond(HttpStatusCode.Unauthorized, "Credentials are not valid")
            }
        }

        session<UserSession>("auth-session") {
            validate { session ->
                session
            }
            challenge {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}

private val userRepository = getKoinInstance<UserRepository>()

@Serializable
data class UserSession(
    val email: String,
    val username: String,
    val userId: Int
)

fun UserSession.getUser(): UserModel? = userRepository.findUserByEmail(this.email)


