package com.saban.pronunciation.model

import com.saban.shared.PaginationRequest
import kotlinx.serialization.Serializable

@Serializable
data class PaginatedPronunciationsRequest(
    override val limit: Int,
    override val offset: Int,
    val language: String?
) : PaginationRequest