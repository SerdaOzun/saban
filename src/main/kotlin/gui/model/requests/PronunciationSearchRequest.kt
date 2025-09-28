package com.saban.gui.model.requests

import kotlinx.serialization.Serializable

@Serializable
data class PronunciationSearchRequest(
    val text: String,
    val language: String
)

@Serializable
data class PronunciationSaveRequest(
    val text: String,
    val language: String
)

