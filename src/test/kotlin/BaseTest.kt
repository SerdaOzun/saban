package com.saban

import com.saban.languages.Language
import com.saban.languages.LanguageRepository.LanguageTable
import com.saban.plugins.SabanConfig
import com.saban.plugins.configureDatabases
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.core.component.KoinComponent
import org.testng.annotations.BeforeClass

open class BaseTest : KoinComponent {

    @BeforeClass
    open fun beforeClass() {
        configureDatabases(SabanConfig())
    }

    fun createLanguages() {
        transaction {
            listOf(
                Language(-1, "german", "de", false),
                Language(-1, "french", "fr", false),
                Language(-1, "arabic", "ar", true),
            ).forEach { lang ->
                LanguageTable.insertIgnore {
                    it[languageName] = lang.name
                    it[languageCode] = lang.code
                    it[rtl] = lang.rtl
                }
            }
        }
    }

}