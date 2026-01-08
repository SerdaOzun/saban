package com.saban.gui

import com.saban.gui.model.SettingsResponse
import com.saban.user.UserRepository
import org.koin.core.component.KoinComponent

class SettingsService(
    private val userRepository: UserRepository
) : KoinComponent {

    fun updateCountry(userId: Int, country: String) = userRepository.updateCountry(userId, country)
    fun getSettings(userId: Int): SettingsResponse = SettingsResponse(userRepository.getCountry(userId))
}