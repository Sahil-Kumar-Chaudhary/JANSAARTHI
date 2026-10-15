package com.example.jansaarthi.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jansaarthi.App
import com.example.jansaarthi.data.model.LoginRequest
import com.example.jansaarthi.data.model.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val authRepository = App.instance.authRepository

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()
    
    fun logout() = authRepository.logout()

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun login(mobile: String, pass: String) {
        if (mobile.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Mobile and password are required.")
            return
        }
        
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.login(LoginRequest(mobile, pass))
            if (result.isSuccess) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun registerFull(
        name: String, mobile: String, email: String?, password: String,
        age: Int, gender: String, state: String, district: String,
        verificationDocType: String, verificationDocUrl: String,
        scCategory: String?, annualFamilyIncome: Int?, occupation: String?,
        businessType: String?, businessRegistrationStatus: String?,
        purpose: String?, requestedAmount: Int?, projectCost: Int?
    ) {
        if (name.isBlank() || mobile.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill mandatory fields.")
            return
        }
        if (!mobile.matches(Regex("^[6-9]\\d{9}$"))) {
            _authState.value = AuthState.Error("Please enter a valid 10-digit Indian mobile number.")
            return
        }
        if (email != null && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("Please enter a valid email address.")
            return
        }
        if (password.length < 8) {
            _authState.value = AuthState.Error("Password must be at least 8 characters.")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val request = RegisterRequest(
                name = name, mobile = mobile, email = email, password = password,
                age = age, gender = gender, state = state, district = district,
                verificationDocType = verificationDocType, verificationDocUrl = verificationDocUrl,
                scCategory = scCategory, annualFamilyIncome = annualFamilyIncome, occupation = occupation,
                businessType = businessType, businessRegistrationStatus = businessRegistrationStatus,
                purpose = purpose, requestedAmount = requestedAmount, projectCost = projectCost
            )
            val result = authRepository.register(request)
            if (result.isSuccess) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }
}
