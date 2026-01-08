package com.saban.languages

import org.koin.core.component.KoinComponent

class LanguageService(private val languageRepository: LanguageRepository) : KoinComponent {

    fun findLanguage(language: String): Language? = languageRepository.read(language)

    fun getLanguages(): List<Language> = languageRepository.getLanguages()
}