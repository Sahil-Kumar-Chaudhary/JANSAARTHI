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
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.DELETE

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

    @Multipart
    @POST("/api/profile/photo")
    suspend fun uploadPhoto(
        @Header("Authorization") token: String,
        @Part photo: okhttp3.MultipartBody.Part
    ): Response<AuthResponse>

    @DELETE("/api/profile/photo")
    suspend fun deletePhoto(@Header("Authorization") token: String): Response<AuthResponse>

    @GET("/api/recommendations")
    suspend fun getRecommendations(@Header("Authorization") token: String): Response<List<com.example.jansaarthi.data.model.Scheme>>
}
