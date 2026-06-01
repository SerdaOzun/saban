package com.saban.gui.model

import kotlinx.serialization.Serializable

@Serializable
data class SettingsResponse(
    val country: String?,
    val spokenLanguages: List<String>
)
