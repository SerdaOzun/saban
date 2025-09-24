package com.saban.core.repository

import com.saban.user.repository.UserRepository
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.javatime.datetime

class VotingRepository {

    object VotesTable : CompositeIdTable("votes") {
        val userId = reference("user_id", UserRepository.UserEntity)
        val pronunciationId = reference("pronunciation_id", PronunciationRepository.PronunciationTable)
        val voteValue = integer("vote_value")
        val createdAt = datetime(name = "created_at")

        override val primaryKey = PrimaryKey(pronunciationId, userId)
    }

}