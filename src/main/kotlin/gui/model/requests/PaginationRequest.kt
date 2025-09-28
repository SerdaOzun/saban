package com.saban.gui.model.requests

import kotlinx.serialization.Serializable

interface PaginationRequest {
    val offset: Int
    val count: Int
}

@Serializable
data class PaginatedPronunciationsRequest(
    override val count: Int,
    override val offset: Int,
    val language: String?
) : PaginationRequest