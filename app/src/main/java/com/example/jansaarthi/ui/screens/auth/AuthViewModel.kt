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

enum class ForgotPasswordStep {
    MOBILE_INPUT,
    OTP_VERIFICATION,
    NEW_PASSWORD,
    SUCCESS
}

data class ForgotPasswordState(
    val step: ForgotPasswordStep = ForgotPasswordStep.MOBILE_INPUT,
    val mobile: String = "",
    val otp: String = "",
    val generatedOtp: String? = null,
    val resetToken: String? = null,
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val mobileError: String? = null,
    val otpError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

class AuthViewModel : ViewModel() {
    private val authRepository = App.instance.authRepository

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow(ForgotPasswordState())
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState.asStateFlow()
    
    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()
    
    fun logout() = authRepository.logout()

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordState()
    }

    fun setForgotMobile(mobile: String) {
        _forgotPasswordState.value = _forgotPasswordState.value.copy(
            mobile = mobile,
            mobileError = null,
            errorMessage = null
        )
    }

    fun setForgotOtp(otp: String) {
        _forgotPasswordState.value = _forgotPasswordState.value.copy(
            otp = otp,
            otpError = null,
            errorMessage = null
        )
    }

    fun setForgotNewPassword(pass: String) {
        _forgotPasswordState.value = _forgotPasswordState.value.copy(
            newPassword = pass,
            passwordError = null,
            errorMessage = null
        )
    }

    fun setForgotConfirmPassword(pass: String) {
        _forgotPasswordState.value = _forgotPasswordState.value.copy(
            confirmPassword = pass,
            confirmPasswordError = null,
            errorMessage = null
        )
    }

    fun toggleForgotNewPasswordVisible() {
        _forgotPasswordState.value = _forgotPasswordState.value.copy(
            isPasswordVisible = !_forgotPasswordState.value.isPasswordVisible
        )
    }

    fun toggleForgotConfirmPasswordVisible() {
        _forgotPasswordState.value = _forgotPasswordState.value.copy(
            isConfirmPasswordVisible = !_forgotPasswordState.value.isConfirmPasswordVisible
        )
    }

    fun sendForgotOtp() {
        val mobile = _forgotPasswordState.value.mobile.trim()
        if (mobile.isEmpty()) {
            _forgotPasswordState.value = _forgotPasswordState.value.copy(mobileError = "Mobile number is required")
            return
        }
        if (!mobile.matches(Regex("^[0-9]{10}$"))) {
            _forgotPasswordState.value = _forgotPasswordState.value.copy(mobileError = "Enter a valid 10-digit mobile number")
            return
        }

        _forgotPasswordState.value = _forgotPasswordState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result = authRepository.forgotPassword(mobile)
            if (result.isSuccess) {
                val data = result.getOrNull()
                _forgotPasswordState.value = _forgotPasswordState.value.copy(
                    isLoading = false,
                    step = ForgotPasswordStep.OTP_VERIFICATION,
                    generatedOtp = data?.otp,
                    successMessage = data?.message ?: "OTP sent successfully"
                )
            } else {
                _forgotPasswordState.value = _forgotPasswordState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to send OTP"
                )
            }
        }
    }

    fun verifyForgotOtp() {
        val mobile = _forgotPasswordState.value.mobile.trim()
        val otp = _forgotPasswordState.value.otp.trim()
        if (otp.isEmpty()) {
            _forgotPasswordState.value = _forgotPasswordState.value.copy(otpError = "Please enter the 6-digit OTP")
            return
        }
        if (otp.length != 6) {
            _forgotPasswordState.value = _forgotPasswordState.value.copy(otpError = "OTP must be 6 digits")
            return
        }

        _forgotPasswordState.value = _forgotPasswordState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result = authRepository.verifyResetOtp(mobile, otp)
            if (result.isSuccess) {
                val data = result.getOrNull()
                _forgotPasswordState.value = _forgotPasswordState.value.copy(
                    isLoading = false,
                    step = ForgotPasswordStep.NEW_PASSWORD,
                    resetToken = data?.resetToken,
                    successMessage = "OTP verified successfully",
                    errorMessage = null
                )
            } else {
                _forgotPasswordState.value = _forgotPasswordState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Invalid OTP"
                )
            }
        }
    }

    fun submitNewPassword() {
        val state = _forgotPasswordState.value
        val pass = state.newPassword
        val confirm = state.confirmPassword

        if (pass.isEmpty()) {
            _forgotPasswordState.value = state.copy(passwordError = "New password is required")
            return
        }
        if (pass.length < 8) {
            _forgotPasswordState.value = state.copy(passwordError = "Password must be at least 8 characters")
            return
        }
        if (!RegistrationFormData.isPasswordStrong(pass)) {
            _forgotPasswordState.value = state.copy(passwordError = "Password must have uppercase, lowercase, and a number")
            return
        }
        if (confirm.isEmpty()) {
            _forgotPasswordState.value = state.copy(confirmPasswordError = "Please confirm your password")
            return
        }
        if (pass != confirm) {
            _forgotPasswordState.value = state.copy(confirmPasswordError = "Passwords do not match")
            return
        }

        _forgotPasswordState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result = authRepository.resetPassword(
                mobile = state.mobile.trim(),
                otp = state.otp.trim(),
                resetToken = state.resetToken,
                newPassword = pass
            )
            if (result.isSuccess) {
                _forgotPasswordState.value = _forgotPasswordState.value.copy(
                    isLoading = false,
                    step = ForgotPasswordStep.SUCCESS,
                    successMessage = result.getOrNull() ?: "Password reset successful!"
                )
            } else {
                _forgotPasswordState.value = _forgotPasswordState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Password reset failed"
                )
            }
        }
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

    fun register(request: RegisterRequest) {
        if (request.name.isBlank() || request.mobile.isBlank() || request.password.isBlank()) {
            _authState.value = AuthState.Error("Please fill mandatory fields.")
            return
        }
        if (!request.mobile.matches(Regex("^[6-9]\\d{9}$"))) {
            _authState.value = AuthState.Error("Please enter a valid 10-digit Indian mobile number.")
            return
        }
        if (request.email != null && !android.util.Patterns.EMAIL_ADDRESS.matcher(request.email).matches()) {
            _authState.value = AuthState.Error("Please enter a valid email address.")
            return
        }
        if (request.password.length < 8) {
            _authState.value = AuthState.Error("Password must be at least 8 characters.")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.register(request)
            if (result.isSuccess) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun registerFull(
        name: String, mobile: String, email: String?, password: String,
        age: Int, gender: String, state: String, district: String,
        verificationDocType: String, verificationDocUrl: String,
        scCategory: String?, annualFamilyIncome: Int?, occupation: String?,
        businessType: String?, businessRegistrationStatus: String?,
        purpose: String?, requestedAmount: Int?, projectCost: Int?,
        educationLevel: String? = null, areaType: String? = null, dob: String? = null
    ) {
        register(
            RegisterRequest(
                name = name, mobile = mobile, email = email, password = password,
                age = age, gender = gender, state = state, district = district,
                verificationDocType = verificationDocType, verificationDocUrl = verificationDocUrl,
                scCategory = scCategory, annualFamilyIncome = annualFamilyIncome, occupation = occupation,
                businessType = businessType, businessRegistrationStatus = businessRegistrationStatus,
                purpose = purpose, requestedAmount = requestedAmount, projectCost = projectCost,
                educationLevel = educationLevel, areaType = areaType, dob = dob
            )
        )
    }
}
