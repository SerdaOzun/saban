package com.saban.gui

import com.saban.gui.model.SettingsResponse
import com.saban.languages.LanguageRepository
import com.saban.user.UserLanguageRepository
import com.saban.user.UserRepository
import org.koin.core.component.KoinComponent

class SettingsService(
    private val userRepository: UserRepository,
    private val languageRepository: LanguageRepository,
    private val userLanguageRepository: UserLanguageRepository
) : KoinComponent {

    fun getSettings(userId: Int): SettingsResponse {
        val userLanguageIds = userLanguageRepository.getUserLanguages(userId)
        val userLanguages = languageRepository.getLanguages().filter { it.id in userLanguageIds }.map { it.name }

        return SettingsResponse(
            userRepository.getCountry(userId),
            userLanguages
        )
    }

    fun updateCountry(userId: Int, country: String) = userRepository.updateCountry(userId, country)
    fun updateSpokenLanguages(userId: Int, languages: Set<String>) {
        val actualLangs = languageRepository.getLanguages().filter { it.name in languages }.map { it.id }
        userLanguageRepository.deleteUserLanguages(userId)
        userLanguageRepository.addLanguages(userId, actualLangs)
    }
}