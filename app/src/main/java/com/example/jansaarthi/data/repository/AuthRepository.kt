package com.example.jansaarthi.data.repository

import com.example.jansaarthi.data.local.TokenManager
import com.example.jansaarthi.data.model.LoginRequest
import com.example.jansaarthi.data.model.RegisterRequest
import com.example.jansaarthi.data.model.User
import com.example.jansaarthi.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class AuthRepository(private val tokenManager: TokenManager) {
    private val api = RetrofitClient.instance

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    suspend fun register(request: RegisterRequest): Result<String> {
        return try {
            val response = api.register(request)
            if (response.isSuccessful) {
                val token = response.body()?.token
                if (token != null) {
                    tokenManager.saveToken(token)
                    _currentUser.value = response.body()?.user
                }
                Result.success("Registration successful")
            } else {
                val errorBody = response.errorBody()?.string()
                val message = extractErrorMessage(errorBody)
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Unable to connect to the server. Please try again."))
        }
    }

    suspend fun login(request: LoginRequest): Result<String> {
        return try {
            val response = api.login(request)
            if (response.isSuccessful) {
                val token = response.body()?.token
                if (token != null) {
                    tokenManager.saveToken(token)
                    _currentUser.value = response.body()?.user
                    Result.success("Login successful")
                } else {
                    Result.failure(Exception("Invalid server response"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val message = extractErrorMessage(errorBody)
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Unable to connect to the server. Please try again."))
        }
    }
    
    suspend fun fetchMe(): Result<User> {
        val token = tokenManager.getToken() ?: return Result.failure(Exception("No token"))
        return try {
            val response = api.getMe("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                _currentUser.value = response.body()
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateProfile(user: User): Result<User> {
        val token = tokenManager.getToken() ?: return Result.failure(Exception("No token"))
        return try {
            val response = api.updateProfile("Bearer $token", user)
            if (response.isSuccessful && response.body()?.user != null) {
                _currentUser.value = response.body()?.user
                Result.success(response.body()?.user!!)
            } else {
                Result.failure(Exception("Failed to update profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isLoggedIn(): Boolean {
        return tokenManager.getToken() != null
    }

    fun logout() {
        tokenManager.clearToken()
        _currentUser.value = null
    }

    private fun extractErrorMessage(errorBody: String?): String {
        return try {
            if (errorBody != null) {
                val json = JSONObject(errorBody)
                json.getString("message")
            } else {
                "An unknown error occurred"
            }
        } catch (e: Exception) {
            "An unknown error occurred"
        }
    }
}
