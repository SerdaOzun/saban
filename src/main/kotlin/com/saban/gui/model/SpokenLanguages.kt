package com.saban.gui.model

import kotlinx.serialization.Serializable

@Serializable
data class SpokenLanguages(
    val languages: List<String> = emptyList(),
)
