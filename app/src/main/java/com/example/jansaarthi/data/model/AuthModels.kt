package com.example.jansaarthi.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    val message: String,
    val token: String?,
    val user: User?
)

data class User(
    @SerializedName("_id") val id: String,
    val name: String,
    val mobile: String,
    val email: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val state: String? = null,
    val district: String? = null,
    val verificationDocType: String? = null,
    val verificationDocUrl: String? = null,
    val profilePhoto: String? = null,
    val scCategory: String? = null,
    val annualFamilyIncome: Int? = null,
    val occupation: String? = null,
    val businessType: String? = null,
    val businessRegistrationStatus: String? = null,
    val purpose: String? = null,
    val requestedAmount: Int? = null,
    val projectCost: Int? = null,
    val language: String = "EN",
    val theme: String = "SYSTEM",
    val notificationsEnabled: Boolean = true
)

data class LoginRequest(
    val mobile: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val mobile: String,
    val email: String? = null,
    val password: String,
    val age: Int,
    val gender: String,
    val state: String,
    val district: String,
    val verificationDocType: String,
    val verificationDocUrl: String,
    val scCategory: String? = null,
    val annualFamilyIncome: Int? = null,
    val occupation: String? = null,
    val businessType: String? = null,
    val businessRegistrationStatus: String? = null,
    val purpose: String? = null,
    val requestedAmount: Int? = null,
    val projectCost: Int? = null
)

