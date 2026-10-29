package com.example.jansaarthi.data.model

import com.google.gson.annotations.SerializedName

/**
 * Request payload for POST /api/rag/ask
 */
data class RagAskRequest(
    @SerializedName("query")
    val query: String,
    
    @SerializedName("k")
    val k: Int = 5,
    
    @SerializedName("mode")
    val mode: String = "hybrid"
)

/**
 * Verified scheme source returned by the RAG pipeline.
 */
data class RagSource(
    @SerializedName("schemeId")
    val schemeId: String = "",
    
    @SerializedName("schemeName")
    val schemeName: String = "",
    
    @SerializedName("category")
    val category: String = "",
    
    @SerializedName("sourcePlatform")
    val sourcePlatform: String = "",
    
    @SerializedName("snippet")
    val snippet: String = "",
    
    @SerializedName("score")
    val score: Double? = null,
    
    @SerializedName("similarity")
    val similarity: Double? = null,
    
    @SerializedName("distance")
    val distance: Double? = null
)

/**
 * Response payload from POST /api/rag/ask
 */
data class RagAskResponse(
    @SerializedName("status")
    val status: String = "",
    
    @SerializedName("query")
    val query: String? = null,
    
    @SerializedName("retrieval_mode")
    val retrievalMode: String? = null,
    
    @SerializedName("answer")
    val answer: String? = null,
    
    @SerializedName("sources_count")
    val sourcesCount: Int = 0,
    
    @SerializedName("sources")
    val sources: List<RagSource> = emptyList(),
    
    @SerializedName("llm_status")
    val llmStatus: String? = null,
    
    @SerializedName("code")
    val code: String? = null,
    
    @SerializedName("message")
    val message: String? = null
)
