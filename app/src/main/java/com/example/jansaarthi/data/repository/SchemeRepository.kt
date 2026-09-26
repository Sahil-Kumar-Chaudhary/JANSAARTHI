package com.example.jansaarthi.data.repository

import com.example.jansaarthi.App
import com.example.jansaarthi.data.model.Scheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SchemeRepository {
    suspend fun getRecommendedSchemes(): List<Scheme> {
        return withContext(Dispatchers.IO) {
            try {
                val token = App.instance.authRepository.isLoggedIn()
                val tokenString = com.example.jansaarthi.data.local.TokenManager(App.instance).getToken()
                if (tokenString != null) {
                    val response = com.example.jansaarthi.data.network.RetrofitClient.instance.getRecommendations("Bearer $tokenString")
                    if (response.isSuccessful && response.body() != null) {
                        return@withContext response.body()!!
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            emptyList()
        }
    }
}
