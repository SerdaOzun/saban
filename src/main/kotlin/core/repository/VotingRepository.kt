package com.saban.core.repository

import com.saban.user.repository.UserRepository
import org.jetbrains.exposed.dao.id.CompositeIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

class VotingRepository {

    object VotesTable : CompositeIdTable("votes") {
        val userId = reference("user_id", UserRepository.UserEntity)
        val pronunciationId = reference("pronunciation_id", PronunciationRepository.PronunciationTable)
        val voteValue = integer("vote_value")
        val createdAt = datetime(name = "created_at")

        override val primaryKey = PrimaryKey(pronunciationId, userId)
    }

}