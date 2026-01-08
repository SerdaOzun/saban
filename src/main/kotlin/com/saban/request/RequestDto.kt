package com.saban.request

import kotlinx.serialization.Serializable

interface PaginationResponse<T> {
    val totalCount: Long
    val data: List<T>
}

@Serializable
data class PaginatedPronunciationResponse(
    override val totalCount: Long,
    override val data: List<RequestEntity>
) : PaginationResponse<RequestEntity>

@Serializable
data class PronunciationSaveRequest(
    val text: String,
    val language: String
)
