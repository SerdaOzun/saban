package com.saban.user.model

import com.saban.user.repository.UserRepository.UserEntity
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
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
        id = resultRow[UserEntity.id].value,
        username = resultRow[UserEntity.username],
        passwordHash = resultRow[UserEntity.passwordHash],
        email = resultRow[UserEntity.email],
        createdAt = resultRow[UserEntity.createdAt],
        updatedAt = resultRow[UserEntity.updatedAt],
        apiToken = resultRow[UserEntity.apiToken]
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
