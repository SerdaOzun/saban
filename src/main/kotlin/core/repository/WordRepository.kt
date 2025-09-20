package com.saban.core.repository

import com.saban.core.model.Word
import com.saban.gui.model.SearchResult
import com.saban.gui.model.requests.SearchRequest
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent

class WordRepository : KoinComponent {
    //Word or sentence
    object WordEntity : IntIdTable("word") {
        val text = text("word_text")
        val language = reference("language_id", LanguageRepository.LanguageTable)
    }

    /**
     * Join WordEntity on LanguageTable and PronunciationTable
     */
    private val joinedMatchesTable = WordEntity.join(
        otherTable = LanguageRepository.LanguageTable,
        joinType = JoinType.INNER,
        onColumn = WordEntity.language,
        otherColumn = LanguageRepository.LanguageTable.id
    ).join(
        otherTable = PronunciationRepository.PronunciationTable,
        joinType = JoinType.LEFT,
        onColumn = WordEntity.id,
        otherColumn = PronunciationRepository.PronunciationTable.wordId
    )

    fun findMatches(searchText: String): Map<String, List<SearchResult>> = transaction {
        joinedMatchesTable.select(WordEntity.id, WordEntity.text, LanguageRepository.LanguageTable.languageName)
            .where { WordEntity.text like "${searchText.trim()}%" }
            .groupBy(
                { it[LanguageRepository.LanguageTable.languageName] },
                { SearchResult(it[WordEntity.text], it[WordEntity.id].value) })
    }

    suspend fun findWord(word: String, language: String): Word? = newSuspendedTransaction {
        WordEntity.join(
            otherTable = LanguageRepository.LanguageTable,
            joinType = JoinType.INNER,
            onColumn = WordEntity.language,
            otherColumn = LanguageRepository.LanguageTable.id
        ).selectAll().where { WordEntity.text eq word and (LanguageRepository.LanguageTable.languageName eq language) }
            .singleOrNull()?.let { Word(it) }
    }

    suspend fun saveWord(word: String, languageId: Int): Int = newSuspendedTransaction {
        WordEntity.insertAndGetId {
            it[WordEntity.text] = word
            it[WordEntity.language] = languageId
        }.value
    }
}