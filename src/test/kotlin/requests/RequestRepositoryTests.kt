package com.saban.requests

import com.saban.BaseTest
import com.saban.languages.LanguageRepository
import com.saban.request.RequestRepository
import com.saban.util.rollbackTransaction
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.testng.annotations.Test
import java.time.OffsetDateTime
import kotlin.getValue

@Test
class RequestRepositoryTests : BaseTest() {

    private val requestRepository by lazy { RequestRepository() }
    private val languageRepository by lazy { LanguageRepository() }

    private var testUserId = -1
    private var germanLanguageId = -1
    private var arabicLanguageId = -1

    override fun beforeClass() {
        super.beforeClass()
        createLanguages()
        testUserId = createTestUser()

        germanLanguageId = languageRepository.read("german")!!.id
        arabicLanguageId = languageRepository.read("arabic")!!.id
    }

    @Test
    fun `saveRequest creates new request with correct data`() {
        rollbackTransaction {
            val word = "Hallo"
            val requestId = requestRepository.saveRequest(
                userId = testUserId,
                word = word,
                langId = germanLanguageId
            )

            requestId shouldBeGreaterThan 0

            requestRepository.read(requestId).apply {
                shouldNotBeNull()
                text shouldBe word
                language shouldBe "german"
                languageId shouldBe germanLanguageId
                requestedBy shouldBe "testuser"
                createdAt.shouldNotBeNull()
            }

            // Verify done flag is false by default
            val requestFromDb = RequestRepository.RequestTable.selectAll()
                .where { RequestRepository.RequestTable.id eq requestId }
                .singleOrNull()
            requestFromDb.shouldNotBeNull()
            requestFromDb[RequestRepository.RequestTable.done].shouldBeFalse()
        }
    }

    @Test
    fun `saveRequest sets createdAt timestamp`() {
        rollbackTransaction {
            val beforeSave = OffsetDateTime.now()
            Thread.sleep(10) // Ensure time difference

            val requestId = requestRepository.saveRequest(
                userId = testUserId,
                word = "Test",
                langId = germanLanguageId
            )

            val afterSave = OffsetDateTime.now()

            val savedRequest = requestRepository.read(requestId)
            savedRequest.shouldNotBeNull()

            savedRequest.createdAt.isAfter(beforeSave).shouldBeTrue()
            savedRequest.createdAt.isBefore(afterSave).shouldBeTrue()
        }
    }

    @Test
    fun `getRequests returns only undone requests`() {
        rollbackTransaction {
            // Create some requests
            requestRepository.saveRequest(testUserId, "Hallo", germanLanguageId)
            requestRepository.saveRequest(testUserId, "مرحبا", arabicLanguageId)

            // Mark one as done
            val doneRequestId = requestRepository.saveRequest(testUserId, "Done", germanLanguageId)
            requestRepository.updateRequestDone(doneRequestId)

            val result = requestRepository.getRequests(offset = 0, count = 10)

            result.totalCount shouldBe 2 // Only undone requests
            result.data shouldHaveSize 2
            result.data.any { it.text == "Done" }.shouldBeFalse()
            result.data.any { it.text == "Hallo" }.shouldBeTrue()
            result.data.any { it.text == "مرحبا" }.shouldBeTrue()
        }
    }

    @Test
    fun `getRequests filters by language when specified`() {
        rollbackTransaction {
            requestRepository.saveRequest(testUserId, "Hallo", germanLanguageId)
            requestRepository.saveRequest(testUserId, "Guten Tag", germanLanguageId)
            requestRepository.saveRequest(testUserId, "مرحبا", arabicLanguageId)

            val germanResult = requestRepository.getRequests(
                language = "german",
                offset = 0,
                count = 10
            )

            germanResult.totalCount shouldBe 2
            germanResult.data.forEach { request ->
                request.language shouldBe "german"
            }

            val arabicResult = requestRepository.getRequests(
                language = "arabic",
                offset = 0,
                count = 10
            )

            arabicResult.totalCount shouldBe 1
            arabicResult.data.first().language shouldBe "arabic"
        }
    }

    @Test
    fun `getRequests returns empty list for non-existent language filter`() {
        rollbackTransaction {
            requestRepository.saveRequest(testUserId, "Hallo", germanLanguageId)

            val result = requestRepository.getRequests(
                language = "spanish",
                offset = 0,
                count = 10
            )

            result.totalCount shouldBe 0
            result.data.shouldBeEmpty()
        }
    }

    @Test
    fun `getRequests respects pagination offset and count`() {
        rollbackTransaction {
            // Create 5 requests
            for (index in 4 downTo 0) {
                requestRepository.saveRequest(
                    testUserId,
                    "Word $index",
                    germanLanguageId
                )
            }

            requestRepository.getRequests(offset = 0, count = 2).apply {
                totalCount shouldBe 5
                data shouldHaveSize 2
                for (i in 0..1) {
                    data[i].text shouldBe "Word $i"
                }
            }

            requestRepository.getRequests(offset = 2, count = 2).apply {
                totalCount shouldBe 5
                data shouldHaveSize 2
                for (i in 2..3) {
                    data[i - 2].text shouldBe "Word $i"
                }
            }

            requestRepository.getRequests(offset = 4, count = 2).apply {
                totalCount shouldBe 5
                data shouldHaveSize 1 // Only one remaining
                data[0].text shouldBe "Word 4"
            }
        }
    }

    @Test
    fun `getRequests orders by createdAt descending`() {
        rollbackTransaction {
            Thread.sleep(10)
            val firstId = requestRepository.saveRequest(testUserId, "First", germanLanguageId)
            Thread.sleep(10)
            val secondId = requestRepository.saveRequest(testUserId, "Second", germanLanguageId)
            Thread.sleep(10)
            val thirdId = requestRepository.saveRequest(testUserId, "Third", germanLanguageId)

            val result = requestRepository.getRequests(offset = 0, count = 10)

            result.data[0].id shouldBe thirdId
            result.data[1].id shouldBe secondId
            result.data[2].id shouldBe firstId
        }
    }

    @Test
    fun `read returns null for non-existent request id`() {
        rollbackTransaction {
            val nonExistentId = 99999
            requestRepository.read(nonExistentId).shouldBeNull()
        }
    }

    @Test
    fun `read returns correct request by id`() {
        rollbackTransaction {
            val word = "UniqueWord"
            val requestId = requestRepository.saveRequest(
                userId = testUserId,
                word = word,
                langId = germanLanguageId
            )

            requestRepository.read(requestId).apply {
                shouldNotBeNull()
                id shouldBe requestId
                text shouldBe word
                requestedBy shouldBe "testuser"
                language shouldBe "german"
                languageId shouldBe germanLanguageId
            }
        }
    }

    @Test
    fun `updateRequestDone marks request as done`() {
        rollbackTransaction {
            val requestId = requestRepository.saveRequest(
                userId = testUserId,
                word = "ToBeDone",
                langId = germanLanguageId
            )

            // Verify initially false
            val requestFromDb = RequestRepository.RequestTable.selectAll()
                .where { RequestRepository.RequestTable.id eq requestId }
                .singleOrNull()
            requestFromDb.shouldNotBeNull()
            requestFromDb[RequestRepository.RequestTable.done].shouldBeFalse()

            // Update to done
            requestRepository.updateRequestDone(requestId)

            // Verify updated
            val updatedRequestFromDb = RequestRepository.RequestTable.selectAll()
                .where { RequestRepository.RequestTable.id eq requestId }
                .singleOrNull()
            updatedRequestFromDb.shouldNotBeNull()
            updatedRequestFromDb[RequestRepository.RequestTable.done].shouldBeTrue()

            // Request should no longer appear in getRequests
            val activeRequests = requestRepository.getRequests(offset = 0, count = 10)
            activeRequests.data.any { it.id == requestId }.shouldBeFalse()
        }
    }

    @Test
    fun `updateRequestDone handles non-existent request gracefully`() {
        rollbackTransaction {
            val nonExistentId = 99999
            requestRepository.updateRequestDone(nonExistentId)
            //requests continue working
            requestRepository.getRequests(offset = 0, count = 10)
        }
    }

    @Test
    fun `getRequests returns complete RequestEntity with all fields populated`() {
        rollbackTransaction {
            val word = "CompleteTest"
            val requestId = requestRepository.saveRequest(
                userId = testUserId,
                word = word,
                langId = arabicLanguageId
            )

            val result = requestRepository.getRequests(offset = 0, count = 10)
            result.data.single { it.id == requestId }.apply {
                id shouldBe requestId
                text shouldBe word
                language shouldBe "arabic"
                languageId shouldBe arabicLanguageId
                requestedBy shouldBe "testuser"
                createdAt.shouldNotBeNull()
            }
        }
    }

    @Test
    fun `multiple requests from same user and language are handled correctly`() {
        rollbackTransaction {
            val words = listOf("Word1", "Word2", "Word3")
            val requestIds = words.map { word ->
                requestRepository.saveRequest(testUserId, word, germanLanguageId)
            }

            val result = requestRepository.getRequests(offset = 0, count = 10)

            result.totalCount shouldBe 3
            result.data.map { it.text }.shouldBe(words.reversed()) // DESC order
            result.data.forEach { request ->
                request.requestedBy shouldBe "testuser"
                request.language shouldBe "german"
            }
            result.data.map { it.id }.shouldContainAll(requestIds)
        }
    }
}