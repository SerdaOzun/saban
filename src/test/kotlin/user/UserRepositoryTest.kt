package com.saban.user

import com.saban.BaseTest
import com.saban.util.SabanResult
import com.saban.util.rollbackTransaction
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.testng.annotations.Test
import java.time.OffsetDateTime

@Test
class UserRepositoryTests : BaseTest() {

    @Test
    fun `saveUser creates user with correct data`() {
        rollbackTransaction {
            val registrationRequest = RegistrationRequest(
                username = "john_doe",
                password = "hashed_password",
                email = "john@example.com"
            )

            val userId = userRepository.saveUser(registrationRequest)
            userId.value shouldBeGreaterThan 0

            userRepository.findUserByEmail("john@example.com")!!.apply {
                username shouldBe "john_doe"
                email shouldBe "john@example.com"
                passwordHash shouldBe "hashed_password"
                createdAt.shouldNotBeNull()
                updatedAt.shouldBeNull()
                apiToken.shouldBeNull()
            }
        }
    }

    @Test
    fun `saveUser sets createdAt timestamp correctly`() {
        rollbackTransaction {
            val beforeSave = OffsetDateTime.now()
            Thread.sleep(10) // Ensure time difference

            val registrationRequest = RegistrationRequest(
                username = "timely_user",
                password = "password123",
                email = "timely@example.com"
            )

            userRepository.saveUser(registrationRequest)
            val afterSave = OffsetDateTime.now()

            val savedUser = userRepository.findUserByEmail("timely@example.com")
            savedUser.shouldNotBeNull()

            savedUser.createdAt.isAfter(beforeSave).shouldBeTrue()
            savedUser.createdAt.isBefore(afterSave).shouldBeTrue()
            savedUser.updatedAt.shouldBeNull()
        }
    }

    @Test
    fun `findUserByEmail returns user when email exists`() {
        rollbackTransaction {
            val registrationRequest = RegistrationRequest(
                username = "existing_user",
                password = "password123",
                email = "existing@example.com"
            )
            userRepository.saveUser(registrationRequest)

            userRepository.findUserByEmail("existing@example.com")!!.apply {
                username shouldBe "existing_user"
                email shouldBe "existing@example.com"
            }
        }
    }

    @Test
    fun `findUserByEmail returns null when email does not exist`() {
        userRepository.findUserByEmail("nonexistent@example.com").shouldBeNull()
    }

    @Test
    fun `findUserByEmail is case-sensitive`() {
        rollbackTransaction {
            val registrationRequest = RegistrationRequest(
                username = "case_sensitive_user",
                password = "password123",
                email = "CaseSensitive@Example.com"
            )
            userRepository.saveUser(registrationRequest)

            // Different case should not find the user
            val wrongCase = userRepository.findUserByEmail("casesensitive@example.com")
            wrongCase.shouldBeNull()

            // Exact case should find the user
            val exactCase = userRepository.findUserByEmail("CaseSensitive@Example.com")
            exactCase.shouldNotBeNull()
        }
    }

    @Test
    fun `checkUsernameOrEmailExists returns SuccessResult when both are available`() {
        rollbackTransaction {
            val result = userRepository.checkUsernameOrEmailExists(
                username = "new_username",
                email = "new@example.com"
            )

            result.shouldBeTypeOf<SabanResult.SuccessResult>()
            result.successMessage shouldBe "Username and Email not in use."
        }
    }

    @Test
    fun `checkUsernameOrEmailExists returns ErrorResult when email already exists`() {
        rollbackTransaction {
            // Create a user with an email
            val registrationRequest = RegistrationRequest(
                username = "user1",
                password = "password123",
                email = "duplicate@example.com"
            )
            userRepository.saveUser(registrationRequest)

            // Check with same email but different username
            val result = userRepository.checkUsernameOrEmailExists(
                username = "user2",
                email = "duplicate@example.com"
            )

            result.shouldBeTypeOf<SabanResult.ErrorResult>()
            result.errorMessage shouldBe "Email 'duplicate@example.com' is already registered"
        }
    }

    @Test
    fun `checkUsernameOrEmailExists returns ErrorResult when username already exists`() {
        rollbackTransaction {
            // Create a user with a username
            val registrationRequest = RegistrationRequest(
                username = "duplicate_user",
                password = "password123",
                email = "unique1@example.com"
            )
            userRepository.saveUser(registrationRequest)

            // Check with same username but different email
            val result = userRepository.checkUsernameOrEmailExists(
                username = "duplicate_user",
                email = "unique2@example.com"
            )

            result.shouldBeTypeOf<SabanResult.ErrorResult>()
            result.errorMessage shouldBe "Username 'duplicate_user' is already in use."
        }
    }

    @Test
    fun `checkUsernameOrEmailExists returns ErrorResult when both username and email exist`() {
        rollbackTransaction {
            // Create a user
            val registrationRequest = RegistrationRequest(
                username = "existing_both",
                password = "password123",
                email = "both@example.com"
            )
            userRepository.saveUser(registrationRequest)

            // Check with same username and email
            val result = userRepository.checkUsernameOrEmailExists(
                username = "existing_both",
                email = "both@example.com"
            )

            result.shouldBeTypeOf<SabanResult.ErrorResult>()
            // Email check happens first, so that error is returned
            result.errorMessage shouldBe "Email 'both@example.com' is already registered"
        }
    }

    @Test
    fun `updateCountry sets country for existing user`() {
        rollbackTransaction {
            // Create a user
            val registrationRequest = RegistrationRequest(
                username = "user_with_country",
                password = "password123",
                email = "country@example.com"
            )
            val userId = userRepository.saveUser(registrationRequest)

            // Initially country should be null
            userRepository.getCountry(userId.value).shouldBeNull()

            userRepository.updateCountry(userId.value, "Germany")

            userRepository.getCountry(userId.value)!! shouldBe "Germany"
        }
    }

    @Test
    fun `updateCountry can change country multiple times`() {
        rollbackTransaction {
            val registrationRequest = RegistrationRequest(
                username = "globetrotter",
                password = "password123",
                email = "traveler@example.com"
            )
            val userId = userRepository.saveUser(registrationRequest)

            userRepository.updateCountry(userId.value, "USA")
            userRepository.getCountry(userId.value) shouldBe "USA"

            userRepository.updateCountry(userId.value, "France")
            userRepository.getCountry(userId.value) shouldBe "France"

            userRepository.updateCountry(userId.value, "Japan")
            userRepository.getCountry(userId.value) shouldBe "Japan"
        }
    }

    @Test
    fun `updateCountry handles non-existent user gracefully`() {
        val nonExistentId = 99999
        userRepository.updateCountry(nonExistentId, "Spain")
        // No assertion needed, just verifying no exception was thrown
    }

    @Test
    fun `getCountry returns null for user without country`() {
        rollbackTransaction {
            val registrationRequest = RegistrationRequest(
                username = "no_country_user",
                password = "password123",
                email = "nocountry@example.com"
            )
            val userId = userRepository.saveUser(registrationRequest)

            userRepository.getCountry(userId.value).shouldBeNull()
        }
    }

    @Test
    fun `getCountry returns null for non-existent user`() {
        val country = userRepository.getCountry(99999)
        country.shouldBeNull()
    }

    @Test
    fun `deleteUser removes user from database`() {
        rollbackTransaction {
            val registrationRequest = RegistrationRequest(
                username = "to_be_deleted",
                password = "password123",
                email = "delete@example.com"
            )
            val userId = userRepository.saveUser(registrationRequest)

            // Verify user exists
            userRepository.findUserByEmail("delete@example.com").shouldNotBeNull()

            // Delete user
            val rowsAffected = userRepository.deleteUser(userId.value)
            rowsAffected shouldBe 1

            // Verify user no longer exists
            userRepository.findUserByEmail("delete@example.com").shouldBeNull()
        }
    }

    @Test
    fun `deleteUser returns 0 for non-existent user`() {
        rollbackTransaction {
            val rowsAffected = userRepository.deleteUser(99999)
            rowsAffected shouldBe 0
        }
    }

    @Test
    fun `saveUser handles multiple users with different data`() {
        rollbackTransaction {
            val users = listOf(
                RegistrationRequest("alice", "pass123456", "alice@example.com"),
                RegistrationRequest("bob", "pass123456", "bob@example.com"),
                RegistrationRequest("charlie", "pass123456", "charlie@example.com")
            )

            val userIds = users.map { userRepository.saveUser(it) }
            userIds.forEach { it.value shouldBeGreaterThan 0 }

            // Verify all users were saved with correct data
            users.forEach { user ->
                val savedUser = userRepository.findUserByEmail(user.email)
                savedUser.shouldNotBeNull()
                savedUser.username shouldBe user.username
                savedUser.email shouldBe user.email
            }

            // Verify all users are distinct
            val distinctEmails = users.map { it.email }.toSet()
            distinctEmails.size shouldBe 3
        }
    }

    @Test
    fun `RegistrationRequest validation methods work correctly`() {
        // Valid username tests
        RegistrationRequest("valid", "pass123456", "email@test.com").validateUsername().shouldBeTrue()
        RegistrationRequest("abcde", "pass123456", "email@test.com").validateUsername().shouldBeTrue()

        // Invalid username tests
        RegistrationRequest("ab", "pass123456", "email@test.com").validateUsername().shouldBeFalse()
        RegistrationRequest("", "pass123456", "email@test.com").validateUsername().shouldBeFalse()
        RegistrationRequest("   ", "pass123456", "email@test.com").validateUsername().shouldBeFalse()

        // Valid email tests
        RegistrationRequest("user", "pass123456", "user@example.com").validateEmail().shouldBeTrue()
        RegistrationRequest("user", "pass123456", "user.name@example.co.uk").validateEmail().shouldBeTrue()

        // Invalid email tests
        RegistrationRequest("user", "pass123456", "invalid-email").validateEmail().shouldBeFalse()
        RegistrationRequest("user", "pass123456", "@example.com").validateEmail().shouldBeFalse()
        RegistrationRequest("user", "pass123456", "").validateEmail().shouldBeFalse()

        // Valid password tests
        RegistrationRequest("user", "password123", "email@test.com").validatePassword().shouldBeTrue()
        RegistrationRequest("user", "longenough1", "email@test.com").validatePassword().shouldBeTrue()

        // Invalid password tests
        RegistrationRequest("user", "short", "email@test.com").validatePassword().shouldBeFalse()
        RegistrationRequest("user", "", "email@test.com").validatePassword().shouldBeFalse()
        RegistrationRequest("user", "1234567", "email@test.com").validatePassword().shouldBeFalse()
    }
}