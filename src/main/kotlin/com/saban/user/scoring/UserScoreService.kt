package com.saban.user.scoring

import org.koin.core.component.KoinComponent
import java.time.OffsetDateTime

class UserScoreService(private val userScoringRepository: UserScoringRepository) : KoinComponent {

    /**
     * Add positive or negative points to a user's score
     */
    fun saveUserScore(request: CreateUserScore) {
        userScoringRepository.addScore(
            request.userId,
            request.pronunciationId,
            request.scoringPoints.points,
            request.scoringPoints,
            request.pointsFrom
        )
    }

    /**
     * Get the total userScore for a user
     * @param userId
     * @param since date after which to consider points. If null, all points are added up
     *
     * @return the sum of all points for the specific user
     */
    fun getTotalUserScore(userId: Int, since: OffsetDateTime? = null) =
        userScoringRepository.getTotalUserScore(userId, since)

    /**
     * Get Users mapped to their points (points descending)
     * @param since date after which to consider points
     * @limit limit to a specific number of users, e.g. top 10, or all if null
     *
     * @return user score board
     */
    fun getUserScores(since: OffsetDateTime, limit: Int? = null): List<UserScoreBoard> =
        userScoringRepository.getUserScoreBoard(since, limit)


}
