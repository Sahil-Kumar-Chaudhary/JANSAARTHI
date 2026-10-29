package com.example.jansaarthi.ui.screens.rag

import com.example.jansaarthi.data.model.RagSource
import java.util.UUID

data class RagMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val sources: List<RagSource> = emptyList(),
    val llmStatus: String? = null,
    val retrievalMode: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class RagUiState(
    val messages: List<RagMessage> = emptyList(),
    val queryInput: String = "",
    val selectedMode: String = "hybrid", // "hybrid", "semantic", "lexical"
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lastFailedQuery: String? = null
)
