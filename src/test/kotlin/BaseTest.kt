package com.saban

import com.saban.languages.Language
import com.saban.languages.LanguageRepository.LanguageTable
import com.saban.plugins.SabanConfig
import com.saban.plugins.configureDatabases
import com.saban.user.RegistrationRequest
import com.saban.user.UserRepository
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.component.KoinComponent
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass

open class BaseTest : KoinComponent {

    val userRepository by lazy { UserRepository() }

    private var testUserId = -1

    @BeforeClass
    open fun beforeClass() {
        configureDatabases(SabanConfig())
    }

    @AfterClass
    open fun afterClass() {
        if (testUserId != -1) {
            userRepository.deleteUser(testUserId)
        }
    }

    fun createLanguages() {
        transaction {
            listOf(
                Language(-1, "german", "de", false),
                Language(-1, "french", "fr", false),
                Language(-1, "arabic", "ar", true),
            ).forEach { lang ->
                LanguageTable.insertIgnore {
                    it[languageName] = lang.name
                    it[languageCode] = lang.code
                    it[rtl] = lang.rtl
                }
            }
        }
    }

    fun createTestUser() = transaction {
        userRepository.saveUser(
            RegistrationRequest(
                username = "testuser",
                email = "test@example.com",
                password = "password123"
            )
        ).value.also { testUserId = it }
    }

}