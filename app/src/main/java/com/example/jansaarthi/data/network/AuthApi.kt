package com.example.jansaarthi.data.network

import com.example.jansaarthi.data.model.AuthResponse
import com.example.jansaarthi.data.model.LoginRequest
import com.example.jansaarthi.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Header

interface AuthApi {
    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @GET("/api/auth/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<com.example.jansaarthi.data.model.User>
    
    @PUT("/api/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String, 
        @Body updates: com.example.jansaarthi.data.model.User
    ): Response<AuthResponse>

    @GET("/api/recommendations")
    suspend fun getRecommendations(@Header("Authorization") token: String): Response<List<com.example.jansaarthi.data.model.Scheme>>
}
