package com.saban.pronunciation

import com.saban.BaseTest
import com.saban.languages.LanguageRepository
import com.saban.util.rollbackTransaction
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.date.shouldBeBefore
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.testng.annotations.Ignore
import org.testng.annotations.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Test
class PronunciationRepositoryTests : BaseTest() {

    private val pronunciationRepository by lazy { PronunciationRepository() }
    private val languageRepository by lazy { LanguageRepository() }

    private var testUserId: Int = -1
    private var germanLangId: Int = -1
    private var arabicLangId: Int = -1

    override fun beforeClass() {
        super.beforeClass()

        transaction {
            testUserId = createTestUser()
            createLanguages()
            germanLangId = languageRepository.read("german")!!.id
            arabicLangId = languageRepository.read("arabic")!!.id

            // Insert test pronunciations
            val testWords = listOf(
                Triple("hallo", germanLangId, "s3://pronunciations/hello_123.mp3"),
                Triple("world", germanLangId, "s3://pronunciations/world_456.mp3"),
                Triple("مرحبا", arabicLangId, "s3://pronunciations/marhaba_789.mp3")
            )

            testWords.forEach { (word, langId, sThreeKey) ->
                PronunciationRepository.PronunciationTable.insert {
                    it[text] = word
                    it[languageId] = langId
                    it[userId] = testUserId
                    it[s3Key] = sThreeKey
                    it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                    it[isApproved] = true
                }
            }
        }
    }

    @Test
    fun `savePronunciation creates new pronunciation record`() {
        rollbackTransaction {
            val newS3Key = "s3://pronunciations/new_word_001.mp3"
            val pronunciationId = pronunciationRepository.savePronunciation(
                userId = testUserId,
                word = "kotlin",
                langId = germanLangId,
                fileKey = newS3Key
            )

            pronunciationId shouldNotBe -1

            val saved = pronunciationRepository.read(pronunciationId)
            saved.shouldNotBeNull()
            saved.text shouldBe "kotlin"
            saved.publicUrl shouldBe newS3Key
            saved.userId shouldBe testUserId
            saved.languageId shouldBe germanLangId
            saved.isApproved shouldBe true
            saved.createdAt.shouldNotBeNull()
        }
    }

    @Test
    fun `savePronunciation creates record with current timestamp`() {
        rollbackTransaction {
            val beforeInsert = OffsetDateTime.now(ZoneOffset.UTC)
            val pronunciationId = pronunciationRepository.savePronunciation(
                userId = testUserId,
                word = "timestamp-test",
                langId = germanLangId,
                fileKey = "s3://test/timestamp.mp3"
            )
            val afterInsert = OffsetDateTime.now(ZoneOffset.UTC)

            val saved = pronunciationRepository.read(pronunciationId)!!

            saved.createdAt shouldBeAfter beforeInsert
            saved.createdAt shouldBeBefore afterInsert
            saved.createdAt shouldNotBe null
        }
    }

    @Test
    fun `searchEntriesByLanguage returns grouped search results`() {
        rollbackTransaction {
            pronunciationRepository.savePronunciation(
                userId = testUserId,
                word = "hallo",
                langId = germanLangId,
                fileKey = "s3://test/timestamp.mp3"
            )

            val results = pronunciationRepository.searchEntriesByLanguage("hallo")

            results shouldNotBe null
            results.containsKey("german") shouldBe true

            val germanResults = results["german"]
            germanResults.shouldNotBeNull()
            germanResults[0].word shouldBe "hallo"
            germanResults[0].wordId shouldBeGreaterThan 0
        }
    }

    @Test
    fun `searchEntriesByLanguage returns empty map for no matches`() {
        rollbackTransaction {
            val results = pronunciationRepository.searchEntriesByLanguage("nonexistentwordxyz")

            results shouldBe emptyMap()
        }
    }

    @Test
    @Ignore("Prüfen ob gewollt ist, dass 'hall' nicht gefunden wird")
    fun `searchEntriesByLanguage handles partial matches`() {
        val results = pronunciationRepository.searchEntriesByLanguage("hall")

        results shouldNotBe null
        results["german"]?.first()?.word shouldBe "hallo"
    }

    @Test
    fun `getPronunciations returns list for specific word and language`() {
        val results = pronunciationRepository.getPronunciations("hallo", "german")

        results[0].username shouldBe "testuser"
        results[0].word shouldBe "hallo"
        results[0].s3key shouldBe "s3://pronunciations/hello_123.mp3"
        results[0].createdAt.shouldNotBeNull()
        results[0].url shouldBe "" // Default empty string
    }

    @Test
    fun `getPronunciations returns empty list for non-existent word`() {
        val results = pronunciationRepository.getPronunciations("xyzabc", "german")
        results shouldBe emptyList()
    }

    @Test
    fun `getPronunciations returns empty list for wrong language`() {
        val results = pronunciationRepository.getPronunciations("hallo", "arabic")
        results shouldBe emptyList()
    }

    @Test
    fun `getPronunciations handles multiple pronunciations of same word`() {
        rollbackTransaction {
            PronunciationRepository.PronunciationTable.insert {
                it[text] = "hallo"
                it[languageId] = germanLangId
                it[userId] = testUserId
                it[s3Key] = "s3://hello_first.mp3"
                it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                it[isApproved] = true
            }

            // Add second pronunciation of "hallo"
            val secondS3Key = "s3://pronunciations/hello_second.mp3"
            PronunciationRepository.PronunciationTable.insert {
                it[text] = "hallo"
                it[languageId] = germanLangId
                it[userId] = testUserId
                it[s3Key] = secondS3Key
                it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                it[isApproved] = true
            }

            val results = pronunciationRepository.getPronunciations("hallo", "german")
            results.map { it.s3key }.first() shouldBe "s3://pronunciations/hello_123.mp3"
        }
    }

    @Test
    fun `read returns correct pronunciation by id`() {
        rollbackTransaction {
            val firstId = PronunciationRepository.PronunciationTable.selectAll()
                .where { PronunciationRepository.PronunciationTable.text eq "hallo" }
                .first()[PronunciationRepository.PronunciationTable.id].value

            pronunciationRepository.read(firstId)!!.apply {
                id shouldBe firstId
                text shouldBe "hallo"
                publicUrl shouldBe "s3://pronunciations/hello_123.mp3"
                languageId shouldBe germanLangId
                isApproved shouldBe true
            }
        }
    }

    @Test
    fun `read returns null for non-existent id`() {
        val result = pronunciationRepository.read(999999)
        result.shouldBeNull()
    }

    @Test
    fun `findWord returns Word object when word exists in language`() {
        pronunciationRepository.findWord("hallo", "german")!!.apply {
            word shouldBe "hallo"
            language shouldBe "german"
            wordId shouldBeGreaterThan 0
        }
    }

    @Test
    fun `findWord is case-insensitive`() {
        val wordLower = pronunciationRepository.findWord("hallo", "german")
        val wordUpper = pronunciationRepository.findWord("HALLO", "german")
        val wordMixed = pronunciationRepository.findWord("HaLlO", "german")

        wordLower.shouldNotBeNull()
        wordUpper.shouldNotBeNull()
        wordMixed.shouldNotBeNull()

        wordLower.wordId shouldBe wordUpper.wordId
        wordLower.word shouldBe wordUpper.word
    }

    @Test
    fun `findWord returns null for word in wrong language`() {
        val result = pronunciationRepository.findWord("hallo", "arabic")
        result.shouldBeNull()
    }

    @Test
    fun `findWord returns null for non-existent word`() {
        val result = pronunciationRepository.findWord("nonexistentword123", "german")
        result.shouldBeNull()
    }

    @Test
    fun `searchEntriesByLanguage handles Arabic RTL text`() {
        val results = pronunciationRepository.searchEntriesByLanguage("مرحبا")!!
        results.containsKey("arabic") shouldBe true
        results["arabic"]?.first()?.word shouldBe "مرحبا"
    }

    @Test
    fun `getPronunciations respects isApproved flag`() {
        rollbackTransaction {
            // Insert unapproved pronunciation
            PronunciationRepository.PronunciationTable.insert {
                it[text] = "unapproved"
                it[languageId] = germanLangId
                it[userId] = testUserId
                it[s3Key] = "s3://pronunciations/unapproved.mp3"
                it[createdAt] = OffsetDateTime.now(ZoneOffset.UTC)
                it[isApproved] = false
            }

            val results = pronunciationRepository.getPronunciations("unapproved", "german")
            results shouldHaveSize 0
        }
    }

    @Test
    fun `savePronunciation handles duplicate entries`() {
        rollbackTransaction {
            val s3Key = "s3://pronunciations/duplicate.mp3"
            val firstId = pronunciationRepository.savePronunciation(
                userId = testUserId,
                word = "duplicate",
                langId = germanLangId,
                fileKey = s3Key
            )

            val secondId = pronunciationRepository.savePronunciation(
                userId = testUserId,
                word = "duplicate",
                langId = germanLangId,
                fileKey = s3Key
            )

            firstId shouldNotBe secondId

            val pronunciations = pronunciationRepository.getPronunciations("duplicate", "german")
            pronunciations shouldHaveSize 2
        }
    }
}