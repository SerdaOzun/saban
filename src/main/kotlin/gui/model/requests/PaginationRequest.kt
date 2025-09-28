package com.saban.gui.model.requests

import kotlinx.serialization.Serializable

interface PaginationRequest {
    val offset: Int
    val limit: Int
}

@Serializable
data class PaginatedPronunciationsRequest(
    override val limit: Int,
    override val offset: Int,
    val language: String?
) : PaginationRequest