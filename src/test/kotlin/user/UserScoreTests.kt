package com.saban.user

import com.saban.BaseTest
import com.saban.languages.LanguageRepository
import com.saban.pronunciation.PronunciationRepository
import com.saban.user.scoring.CreateUserScore
import com.saban.user.scoring.ScorePoints
import com.saban.user.scoring.UserScoreService
import com.saban.user.scoring.UserScoringRepository
import com.saban.util.rollbackTransaction
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.time.OffsetDateTime
import kotlin.random.Random

class UserScoreTests : BaseTest() {

    private var userId = -1
    private var user2Id = -1
    private var germanLangId: Int = -1

    private val languageRepository by lazy { LanguageRepository() }
    private val pronunciationRepository by lazy { PronunciationRepository() }
    private val userScoreRepository by lazy { UserScoringRepository() }
    private val userScoreService by lazy { UserScoreService(userScoreRepository) }

    @BeforeClass
    override fun beforeClass() {
        super.beforeClass()
        userId = createTestUser()
        user2Id = createTestUser()
        createLanguages()

        transaction {
            germanLangId = languageRepository.read("german")!!.id
        }
    }

    @AfterClass
    override fun afterClass() {
        super.afterClass()
    }

    @Test
    fun `Saving user score for user`() = rollbackTransaction {
        withClue("A new user should have zero points") {
            userScoreService.getTotalUserScore(userId) shouldBe 0
        }

        withClue("Points can be added to a user and increase their points") {
            userScoreService.saveUserScore(createPronunciationScore())
            userScoreService.getTotalUserScore(userId) shouldBe ScorePoints.PRONUNCIATION.points
        }
    }

    @Test
    fun `Users can have negative points`() = rollbackTransaction {
        userScoreService.saveUserScore(createDownvoteScore())
        userScoreService.getTotalUserScore(userId) shouldBe ScorePoints.DOWNVOTE.points
    }

    @Test
    fun `Scores can add up`() = rollbackTransaction {
        userScoreService.saveUserScore(createUpvoteScore())
        userScoreService.saveUserScore(createPronunciationScore())

        userScoreService.getTotalUserScore(userId) shouldBe listOf(
            ScorePoints.UPVOTE.points,
            ScorePoints.PRONUNCIATION.points,
        ).sum()
    }

    @Test
    fun `Scores can be filtered with a 'since' parameter`() = rollbackTransaction {
        userScoreService.saveUserScore(createUpvoteScore())
        val since = OffsetDateTime.now()
        userScoreService.saveUserScore(createPronunciationScore())

        userScoreService.getTotalUserScore(userId, since) shouldBe ScorePoints.PRONUNCIATION.points
    }

    @Test
    fun `Users can upvote and downvote each other`() = rollbackTransaction {
        withClue("Users can up/downvote a pronunciation only once") {
            val pronunciationId = createPronunciation()
            listOf(
                createUpvoteScore(pronunciationId),
                createUpvoteScore(pronunciationId),
                createDownvoteScore(pronunciationId),
                createPronunciationScore(pronunciationId),
            ).forEach {
                userScoreService.saveUserScore(it)
            }

            //upvote + upvote + downvote = -1 point + Pronunciation = 1 point
            userScoreService.getTotalUserScore(userId) shouldBe 1
        }
    }

    @Test
    fun `Multiple upvotes count only once`() = rollbackTransaction {
        val pronunciationId = createPronunciation()
        listOf(
            createUpvoteScore(pronunciationId),
            createUpvoteScore(pronunciationId),
            createUpvoteScore(pronunciationId),
        ).forEach {
            userScoreService.saveUserScore(it)
        }

        userScoreService.getTotalUserScore(userId) shouldBe ScorePoints.UPVOTE.points
    }

    @Test
    fun `Database unique index on pronunciation + pointsFrom allows null user`() = rollbackTransaction {
        withClue("Up and downvotes only count once and get overridden each time") {
            val pronunciationId = createPronunciation()
            userScoreService.saveUserScore(createUpvoteScore(pronunciationId))
            userScoreService.saveUserScore(createPronunciationScore(pronunciationId))

            userScoreService.getTotalUserScore(userId) shouldBe
                    ScorePoints.PRONUNCIATION.points + ScorePoints.UPVOTE.points
        }
    }

    private fun createUpvoteScore(pronunciationId: Int = createPronunciation()): CreateUserScore =
        CreateUserScore(userId, pronunciationId, ScorePoints.UPVOTE, user2Id)

    private fun createDownvoteScore(pronunciationId: Int = createPronunciation()): CreateUserScore =
        CreateUserScore(userId, pronunciationId, ScorePoints.DOWNVOTE, user2Id)

    private fun createPronunciationScore(pronunciationId: Int = createPronunciation()): CreateUserScore =
        CreateUserScore(userId, pronunciationId, ScorePoints.PRONUNCIATION)

    private fun createPronunciation(): Int {
        return pronunciationRepository.savePronunciation(
            userId = userId,
            word = "kotlin${Random.nextInt()}",
            langId = germanLangId,
            fileKey = "s3://pronunciations/new_word_001.mp3"
        )
    }
}