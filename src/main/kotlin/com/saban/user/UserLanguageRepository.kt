package com.saban.user

import com.saban.languages.LanguageRepository
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.component.KoinComponent

class UserLanguageRepository : KoinComponent {

    object UserLanguageTable : Table("user_language") {
        val userId = reference("user_id", UserRepository.UserEntity.id)
        val languageId = reference("language_id", LanguageRepository.LanguageTable.id)

        override val primaryKey = PrimaryKey(userId, languageId)
    }

    fun addLanguage(userId: Int, languageId: Int) = transaction{
        UserLanguageTable.insert {
            it[UserLanguageTable.userId] = userId
            it[UserLanguageTable.languageId] = languageId
        }
    }

    fun addLanguages(userId: Int, languageIds: List<Int>) = transaction {
        UserLanguageTable.batchInsert(languageIds) { langId ->
            this[UserLanguageTable.userId] = userId
            this[UserLanguageTable.languageId] = langId
        }
    }

    fun deleteUserLanguages(userId: Int) = transaction {
        UserLanguageTable.deleteWhere { UserLanguageTable.userId eq userId }
    }

    fun getUserLanguages(userId: Int) = transaction {
        UserLanguageTable.select(UserLanguageTable.languageId).where { UserLanguageTable.userId eq userId }
            .map { it[UserLanguageTable.languageId].value }
    }
}