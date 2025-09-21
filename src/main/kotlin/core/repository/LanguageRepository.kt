package com.saban.core.repository

import com.saban.core.model.Language
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent

class LanguageRepository : KoinComponent {

    object LanguageTable : IntIdTable("saban_language") {
        val languageName = text("language_name")
        val languageCode = text("language_code")
        val rtl = bool("rtl") //right to left language
    }

    private val languageMap: Map<String, Language> = transaction {
        LanguageTable.selectAll()
            .associate { it[LanguageTable.languageName] to Language(it) }
    }

    fun getLanguages(): List<Language> {
        return languageMap.values.toList()
    }

    fun read(language: String): Language? {
        return languageMap[language] ?: transaction {
            LanguageTable.selectAll().where { LanguageTable.languageName eq language }
                .singleOrNull()?.let { Language(it) }
        }
    }
}