package com.saban.gui.model.requests

import kotlinx.serialization.Serializable

@Serializable
data class SearchRequest(
    val searchWord: String
)