package com.saban.gui.model.requests

import kotlinx.serialization.Serializable

@Serializable
data class PronunciationRequest(
    val text: String,
    val language: String
)

