package com.saban.gui.model

import com.saban.core.model.core.model.PronunciationRequest
import kotlinx.serialization.Serializable

interface PaginationResponse<T> {
    val totalCount: Long
    val data: List<T>
}

@Serializable
data class PaginatedPronunciationResponse(
    override val totalCount: Long,
    override val data: List<PronunciationRequest>
) : PaginationResponse<PronunciationRequest>