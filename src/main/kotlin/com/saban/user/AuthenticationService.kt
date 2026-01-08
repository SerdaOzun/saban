package com.saban.user

import at.favre.lib.crypto.bcrypt.BCrypt
import com.saban.plugins.SabanConfig
import com.saban.util.SabanResult
import org.koin.core.component.KoinComponent

class AuthenticationService(
    private val config: SabanConfig,
    private val userRepository: UserRepository
) : KoinComponent {

    fun createAdminUser() {
        config.security.adminUser.let { admin ->
            val adminRegistration = RegistrationRequest(admin.username, admin.password, admin.email)
            registerUser(adminRegistration)
        }
    }

    fun registerUser(registrationRequest: RegistrationRequest): SabanResult {
        val validationResult = runNewUserValidation(registrationRequest)

        return if (validationResult is SabanResult.ErrorResult) {
            validationResult
        } else {
            val hashedPassword = BCrypt.withDefaults().hashToString(12, registrationRequest.password.toCharArray())
            userRepository.saveUser(registrationRequest.copy(password = hashedPassword))
            SabanResult.SuccessResult("User created. You can now login")
        }
    }

    fun login(email: String, password: String): UserModel? {
        return userRepository.findUserByEmail(email)?.let { registeredUser ->
            val bcryptResult = BCrypt.verifyer().verify(password.toCharArray(), registeredUser.passwordHash)
            if (bcryptResult.verified) {
                registeredUser
            } else {
                null
            }
        }
    }

    private fun runNewUserValidation(registrationRequest: RegistrationRequest): SabanResult {
        return when {
            !registrationRequest.validateUsername() -> SabanResult.ErrorResult("Username must have at least 3 characters")
            !registrationRequest.validateEmail() -> SabanResult.ErrorResult("Please provide a valid email")
            !registrationRequest.validatePassword() -> SabanResult.ErrorResult("Please provide a password with at least 8 characters")
            else -> {
                val existingUserValidation = userRepository.checkUsernameOrEmailExists(
                    registrationRequest.username,
                    registrationRequest.email
                )
                existingUserValidation as? SabanResult.ErrorResult ?: SabanResult.SuccessResult("Validation passed")
            }
        }
    }

}