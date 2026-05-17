package com.saban.languages

import com.saban.BaseTest
import com.saban.languages.LanguageRepository.LanguageTable
import com.saban.module
import com.saban.plugins.SabanConfig
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.component.inject
import org.testng.annotations.BeforeClass
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

@Test
class LanguageRepositoryTests : BaseTest() {

    private val repository by lazy { LanguageRepository() }

    override fun beforeClass() {
        super.beforeClass()
        createLanguages()
    }

    @Test
    fun `getLanguages returns all languages`() {
        transaction {
            try {
                val languages = repository.getLanguages()

                languages.size shouldBe 3
                languages.any { it.name == "french" }.shouldBeTrue()
                languages.any { it.name == "german" }.shouldBeTrue()
                languages.any { it.name == "arabic" }.shouldBeTrue()

                // Verify RTL property
                languages.find { it.name == "arabic" }!!.rtl.shouldBeTrue()
                languages.find { it.name == "german" }!!.rtl.shouldBeFalse()
            } finally {
                rollback()
            }
        }
    }

    @Test
    fun `read returns existing language by name`() {
        transaction {
            try {
                repository.read("english")?.apply {
                    shouldNotBeNull()
                    name shouldBe "english"
                    code shouldBe "en"
                    rtl.shouldBeFalse()
                }

                repository.read("arabic")?.apply {
                    shouldNotBeNull()
                    name shouldBe "arabic"
                    code shouldBe "ar"
                    rtl.shouldBeTrue()
                }
            } finally {
                rollback()
            }
        }
    }

    @Test
    fun `read returns null for non-existent language`() {
        transaction {
            try {
                repository.read("spanish").shouldBeNull()
                repository.read("").shouldBeNull()
            } finally {
                rollback()
            }
        }
    }

    @Test
    fun `read with case sensitivity`() {
        transaction {
            try {
                // Repository should be case-sensitive as per current implementation
                repository.read("German").shouldBeNull()
                repository.read("german").shouldNotBeNull()
            } finally {
                rollback()
            }
        }
    }

    @Test
    fun `read query when language not in cache`() {
        transaction {
            try {
                repository.read("Spanish").shouldBeNull()

                val newId = LanguageTable.insertAndGetId {
                    it[languageName] = "Spanish"
                    it[languageCode] = "esp"
                    it[rtl] = false
                }

                repository.read("Spanish").apply {
                    shouldNotBeNull()
                    id shouldBe newId.value
                    name shouldBe "Spanish"
                    code shouldBe "esp"
                    rtl.shouldBeFalse()
                }
            } finally {
                rollback()
            }
        }
    }

}