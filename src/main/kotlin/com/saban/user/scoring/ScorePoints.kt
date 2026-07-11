package com.saban.user.scoring

enum class ScorePoints(val points: Int) {
    UPVOTE(points = 1),
    DOWNVOTE(points = -1),
    PRONUNCIATION(points = 2),
}