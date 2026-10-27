package com.example.jansaarthi.data.repository

import com.example.jansaarthi.data.model.RagAskRequest
import com.example.jansaarthi.data.model.RagAskResponse
import com.example.jansaarthi.data.network.RagApi
import com.example.jansaarthi.data.network.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class RagRepository(
    private val api: RagApi = RetrofitClient.ragApi,
    private val gson: Gson = Gson(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun askQuestion(
        query: String,
        k: Int = 5,
        mode: String = "hybrid"
    ): Result<RagAskResponse> = withContext(ioDispatcher) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Query cannot be empty."))
        }
        if (trimmed.length > 1000) {
            return@withContext Result.failure(IllegalArgumentException("Query must not exceed 1000 characters."))
        }

        try {
            val response = api.askQuestion(
                RagAskRequest(
                    query = trimmed,
                    k = k,
                    mode = mode
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(IllegalStateException("Received empty response body from RAG service."))
                }
            } else {
                val code = response.code()
                val errorBodyStr = response.errorBody()?.string()
                val parsedError = try {
                    if (!errorBodyStr.isNullOrEmpty()) {
                        gson.fromJson(errorBodyStr, RagAskResponse::class.java)
                    } else null
                } catch (e: Exception) {
                    null
                }

                val errorMessage = when (code) {
                    400 -> parsedError?.message ?: "Invalid query. Please verify your question and try again."
                    503 -> parsedError?.message ?: "Scheme knowledge base is currently preparing or temporarily unavailable. Please retry in a few moments."
                    else -> parsedError?.message ?: "Server returned error (HTTP $code). Please try again."
                }

                Result.failure(IOException(errorMessage))
            }
        } catch (e: IOException) {
            Result.failure(IOException("Network error connecting to JANSAARTHI service. Please check your internet connection.", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
