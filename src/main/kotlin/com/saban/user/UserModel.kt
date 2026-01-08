package com.saban.user

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.OffsetDateTime

//android/util/Patterns.java EMAIL_ADDRESS pattern
private val emailAddressRegex = Regex(
    "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"
)

data class UserModel(
    val id: Int,
    val username: String,
    val passwordHash: String,
    val email: String,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime?,
    val apiToken: String?
) {
    constructor(resultRow: ResultRow) : this(
        id = resultRow[UserRepository.UserEntity.id].value,
        username = resultRow[UserRepository.UserEntity.username],
        passwordHash = resultRow[UserRepository.UserEntity.passwordHash],
        email = resultRow[UserRepository.UserEntity.email],
        createdAt = resultRow[UserRepository.UserEntity.createdAt],
        updatedAt = resultRow[UserRepository.UserEntity.updatedAt],
        apiToken = resultRow[UserRepository.UserEntity.apiToken]
    )
}

@Serializable
data class RegistrationRequest(
    val username: String,
    val password: String,
    val email: String
) {
    fun validateUsername() = username.isNotBlank() && username.length > 3
    fun validateEmail() = email.matches(emailAddressRegex)
    fun validatePassword() = password.isNotBlank() && password.length >= 8
}
