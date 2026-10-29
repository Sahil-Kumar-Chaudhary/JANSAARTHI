package com.example.jansaarthi.data.network

import com.example.jansaarthi.data.model.RagAskRequest
import com.example.jansaarthi.data.model.RagAskResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API interface for JANSAARTHI RAG endpoints.
 */
interface RagApi {
    @POST("/api/rag/ask")
    suspend fun askQuestion(@Body request: RagAskRequest): Response<RagAskResponse>
}
