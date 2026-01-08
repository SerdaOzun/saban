package com.saban.user

import com.saban.util.SabanResult
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.koin.core.component.KoinComponent
import java.time.OffsetDateTime
import java.time.ZoneOffset

class UserRepository : KoinComponent {

    object UserEntity : IntIdTable("saban_user") {
        val username = text("username")
        val passwordHash = text("password_hash")
        val email = text("email")
        val createdAt = timestampWithTimeZone(name = "created_at")
        val updatedAt = timestampWithTimeZone(name = "updated_at").nullable()
        val apiToken = text("api_token").nullable()
        val country = text("country").nullable()
    }

    fun findUserByEmail(email: String): UserModel? = transaction {
        UserEntity.selectAll().where { UserEntity.email eq email }.singleOrNull()?.let {
            UserModel(it)
        }
    }

    fun checkUsernameOrEmailExists(username: String, email: String): SabanResult = transaction {
        val emailExists = UserEntity.select(UserEntity.id).where { UserEntity.email eq email }.empty().not()
        if (emailExists) {
            return@transaction SabanResult.ErrorResult("Email '$email' is already registered")
        }

        val userExists = UserEntity.select(UserEntity.id).where { UserEntity.username eq username }.empty().not()
        if (userExists) {
            SabanResult.ErrorResult("Username '$username' is already in use.")
        } else {
            SabanResult.SuccessResult("Username and Email not in use.")
        }
    }

    fun saveUser(user: RegistrationRequest) = transaction {
        UserEntity.insert {
            it[username] = user.username
            it[passwordHash] = user.password
            it[email] = user.email
            it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
            it[updatedAt] = null
            it[country] = null
        }
    }

    fun updateCountry(userId: Int, country: String) = transaction {
        UserEntity.update(where = { UserEntity.id eq userId }) {
            it[UserEntity.country] = country
        }
    }

    fun getCountry(userId: Int): String? = transaction {
        UserEntity.select(UserEntity.country).where { UserEntity.id eq userId }.singleOrNull()?.let {
            it[UserEntity.country]
        }
    }
}