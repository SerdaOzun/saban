package com.saban.pronunciation.model

import kotlinx.serialization.Serializable

@Serializable
data class PronunciationSearchRequest(
    val text: String,
    val language: String
)