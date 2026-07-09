package com.saban.user

import com.saban.BaseTest
import com.saban.gui.SettingsService
import com.saban.languages.LanguageRepository
import com.saban.util.rollbackTransaction
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

class UserSettingsTest : BaseTest() {

    private val languageRepository by lazy { LanguageRepository() }
    private val userLanguageRepository by lazy { UserLanguageRepository() }
    private val settings by lazy { SettingsService(userRepository, languageRepository, userLanguageRepository) }

    private var testUserId = -1

    @BeforeClass
    override fun beforeClass() {
        super.beforeClass()
        transaction {
            testUserId = createTestUser()
            createLanguages()
        }
    }

    @Test
    fun `Add new spoken languages to user`() {
        rollbackTransaction {
            withClue("Initially the user has no spoken languages") {
                settings.getSettings(testUserId).spokenLanguages.shouldBeEmpty()
            }

            withClue("When adding spoken languages, only existing once are actually added") {
                settings.updateSpokenLanguages(testUserId, setOf("german", "french", "fakelang"))
                settings.getSettings(testUserId).spokenLanguages.apply {
                    size shouldBe 2
                    shouldContainExactlyInAnyOrder("french", "german")
                }
            }

            withClue("Updating spoken languages only keeps the new ones") {
                settings.updateSpokenLanguages(testUserId, setOf("german", "arabic"))
                settings.getSettings(testUserId).spokenLanguages.apply {
                    size shouldBe 2
                    shouldContainExactlyInAnyOrder("arabic", "german")
                }
            }
        }
    }
}