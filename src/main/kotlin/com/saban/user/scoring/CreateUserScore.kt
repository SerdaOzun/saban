package com.saban.user.scoring

import com.saban.user.UserId
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.sum

data class CreateUserScore(
    val userId: UserId,
    val pronunciationId: Int,
    val scoringPoints: ScorePoints,
    val pointsFrom: UserId? = null
)

data class UserScoreBoard(
    val userId: UserId,
    val points: Int
) {
    constructor(resultRow: ResultRow) : this(
        resultRow[UserScoringRepository.UserScoringTable.userId].value,
        resultRow[UserScoringRepository.UserScoringTable.points.sum()]!!,
    )
}