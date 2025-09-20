package com.saban.gui.model.requests

import kotlinx.serialization.Serializable

@Serializable
data class PronunciationRequest(
    val searchText: String,
    val language: String
)

