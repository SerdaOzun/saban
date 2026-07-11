package com.saban.user.scoring

import com.saban.pronunciation.PronunciationRepository
import com.saban.user.UserId
import com.saban.user.UserRepository
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.sum
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert
import org.koin.core.component.KoinComponent
import java.time.OffsetDateTime

class UserScoringRepository : KoinComponent {

    object UserScoringTable : IntIdTable("saban_scoring") {
        val userId = reference("user_id", UserRepository.UserEntity.id)
        val pronunciationId = reference("pronunciation_id", PronunciationRepository.PronunciationTable.id)
        val points = integer("points")
        val reason = enumerationByName<ScorePoints>("reason", 16)
        val pointsFrom = reference("points_from", UserRepository.UserEntity.id).nullable()
        val createdAt = timestampWithTimeZone("created_at")
    }

    fun addScore(
        userId: UserId, pronunciationId: Int, points: Int,
        reason: ScorePoints, pointsFromUser: UserId? = null
    ) = transaction {
        UserScoringTable.upsert(keys = arrayOf(UserScoringTable.pronunciationId, UserScoringTable.pointsFrom)) {
            it[UserScoringTable.userId] = userId
            it[UserScoringTable.pronunciationId] = pronunciationId
            it[UserScoringTable.points] = points
            it[UserScoringTable.reason] = reason
            it[UserScoringTable.pointsFrom] = pointsFromUser
            it[UserScoringTable.createdAt] = OffsetDateTime.now()
        }
    }

    fun getTotalUserScore(userId: UserId, since: OffsetDateTime?): Int = transaction {
        val pointsSum = UserScoringTable.points.sum()

        val query = UserScoringTable.select(pointsSum)
            .where { UserScoringTable.userId eq userId }

        since?.let {
            query.andWhere { UserScoringTable.createdAt greaterEq since }
        }

        query.firstOrNull()
            ?.let { it[pointsSum] }
            ?: 0
    }

    fun getUserScoreBoard(since: OffsetDateTime, limit: Int?): List<UserScoreBoard> = transaction {
        var query = UserScoringTable.selectAll()
            .where { UserScoringTable.createdAt greaterEq since }

        limit?.let {
            query = query.limit(it)
        }

        query.map(::UserScoreBoard)
    }

    fun deleteScore(scoreId: Int) = transaction {
        UserScoringTable.deleteWhere { UserScoringTable.id eq scoreId }
    }
}