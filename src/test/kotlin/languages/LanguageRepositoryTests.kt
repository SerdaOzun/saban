package com.saban.languages

import com.saban.BaseTest
import com.saban.languages.LanguageRepository.LanguageTable
import com.saban.util.rollbackTransaction
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
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
        rollbackTransaction {
            val languages = repository.getLanguages()

            languages.size shouldBe 3
            languages.any { it.name == "french" }.shouldBeTrue()
            languages.any { it.name == "german" }.shouldBeTrue()
            languages.any { it.name == "arabic" }.shouldBeTrue()

            // Verify RTL property
            languages.find { it.name == "arabic" }!!.rtl.shouldBeTrue()
            languages.find { it.name == "german" }!!.rtl.shouldBeFalse()
        }
    }

    @Test
    fun `read returns existing language by name`() {
        rollbackTransaction {
            repository.read("german")!!.apply {
                shouldNotBeNull()
                name shouldBe "german"
                code shouldBe "de"
                rtl.shouldBeFalse()
            }

            repository.read("arabic")!!.apply {
                shouldNotBeNull()
                name shouldBe "arabic"
                code shouldBe "ar"
                rtl.shouldBeTrue()
            }
        }
    }

    @Test
    fun `read returns null for non-existent language`() {
        rollbackTransaction {
            repository.read("spanish").shouldBeNull()
            repository.read("").shouldBeNull()
        }
    }

    @Test
    fun `read with case sensitivity`() {
        rollbackTransaction {
            // Repository should be case-sensitive as per current implementation
            repository.read("German").shouldBeNull()
            repository.read("german").shouldNotBeNull()
        }
    }

    @Test
    fun `read query when language not in cache`() {
        rollbackTransaction {
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
        }
    }

}