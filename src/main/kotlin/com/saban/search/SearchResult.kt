package com.saban.search

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(
    val word: String,
    val wordId: Int
)