package com.example.jansaarthi.ui.screens.auth

import android.util.Patterns
import com.example.jansaarthi.data.model.RegisterRequest
import com.example.jansaarthi.data.model.RegistrationOptions
import java.time.LocalDate
import java.time.Period
import java.util.regex.Pattern

enum class RegistrationStep {
    INTRO,
    BASIC_DETAILS,
    ADDITIONAL_DETAILS,
    CREATE_PASSWORD
}

data class RegistrationFormData(
    val step: RegistrationStep = RegistrationStep.INTRO,

    // Step 1: Basic Details
    val fullName: String = "",
    val mobileNumber: String = "",
    val email: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val state: String = "",
    val district: String = "",
    val category: String = "",
    val termsAccepted: Boolean = false,

    // Step 2: Additional Details
    val occupation: String = "",
    val annualIncomeRange: String = "",
    val educationLevel: String = "",
    val areaType: String = "Urban",

    // Step 3: Create Password
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,

    // Field-level errors
    val fullNameError: String? = null,
    val mobileError: String? = null,
    val emailError: String? = null,
    val dobError: String? = null,
    val genderError: String? = null,
    val stateError: String? = null,
    val districtError: String? = null,
    val categoryError: String? = null,
    val termsError: String? = null,
    val occupationError: String? = null,
    val incomeError: String? = null,
    val educationError: String? = null,
    val areaError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
) {

    companion object {
        private val EMAIL_REGEX = Pattern.compile(
            "[a-zA-Z0-9+._%\\-]{1,256}" +
                    "@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )

        fun isMinLengthMet(password: String): Boolean = password.length >= 8
        fun isUpperCaseMet(password: String): Boolean = password.any { it.isUpperCase() }
        fun isLowerCaseMet(password: String): Boolean = password.any { it.isLowerCase() }
        fun isDigitMet(password: String): Boolean = password.any { it.isDigit() }

        fun isPasswordStrong(password: String): Boolean {
            return isMinLengthMet(password) &&
                    isUpperCaseMet(password) &&
                    isLowerCaseMet(password) &&
                    isDigitMet(password)
        }

        fun calculateAge(dobStr: String): Int {
            return try {
                val clean = dobStr.trim()
                val parts = clean.split("/", "-")
                if (parts.size == 3) {
                    val (day, month, year) = if (parts[0].length == 4) {
                        Triple(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
                    } else {
                        Triple(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
                    }
                    val birthDate = LocalDate.of(year, month, day)
                    val today = LocalDate.now()
                    Period.between(birthDate, today).years
                } else {
                    25
                }
            } catch (e: Exception) {
                25
            }
        }
    }

    fun validateStep1(): Pair<Boolean, RegistrationFormData> {
        var hasError = false
        var nameErr: String? = null
        var phoneErr: String? = null
        var mailErr: String? = null
        var bdayErr: String? = null
        var sexErr: String? = null
        var stErr: String? = null
        var distErr: String? = null
        var catErr: String? = null
        var termErr: String? = null

        val trimmedName = fullName.trim()
        if (trimmedName.isEmpty()) {
            nameErr = "Full name is required"
            hasError = true
        } else if (trimmedName.length < 2) {
            nameErr = "Name must be at least 2 characters"
            hasError = true
        }

        val trimmedMobile = mobileNumber.trim()
        if (trimmedMobile.isEmpty()) {
            phoneErr = "Mobile number is required"
            hasError = true
        } else if (!trimmedMobile.matches(Regex("^[6-9]\\d{9}$"))) {
            phoneErr = "Enter a valid 10-digit Indian mobile number"
            hasError = true
        }

        val trimmedEmail = email.trim()
        if (trimmedEmail.isNotEmpty() && !EMAIL_REGEX.matcher(trimmedEmail).matches()) {
            mailErr = "Enter a valid email address"
            hasError = true
        }

        val trimmedDob = dateOfBirth.trim()
        if (trimmedDob.isEmpty()) {
            bdayErr = "Date of birth is required"
            hasError = true
        } else {
            val age = calculateAge(trimmedDob)
            if (age < 18) {
                bdayErr = "Applicant must be at least 18 years old"
                hasError = true
            } else if (age > 120) {
                bdayErr = "Enter a valid date of birth"
                hasError = true
            }
        }

        if (gender.isBlank()) {
            sexErr = "Please select gender"
            hasError = true
        }

        if (state.isBlank()) {
            stErr = "Please select state"
            hasError = true
        }

        if (district.isBlank()) {
            distErr = "Please select district"
            hasError = true
        }

        if (category.isBlank()) {
            catErr = "Please select category"
            hasError = true
        }

        if (!termsAccepted) {
            termErr = "Please agree to Terms & Conditions"
            hasError = true
        }

        val updated = this.copy(
            fullNameError = nameErr,
            mobileError = phoneErr,
            emailError = mailErr,
            dobError = bdayErr,
            genderError = sexErr,
            stateError = stErr,
            districtError = distErr,
            categoryError = catErr,
            termsError = termErr
        )

        return Pair(!hasError, updated)
    }

    fun validateStep2(): Pair<Boolean, RegistrationFormData> {
        var hasError = false
        var occErr: String? = null
        var incErr: String? = null
        var eduErr: String? = null
        var arErr: String? = null

        if (occupation.isBlank()) {
            occErr = "Please select occupation"
            hasError = true
        }

        if (annualIncomeRange.isBlank()) {
            incErr = "Please select annual income"
            hasError = true
        }

        if (educationLevel.isBlank()) {
            eduErr = "Please select education level"
            hasError = true
        }

        if (areaType.isBlank()) {
            arErr = "Please select area type"
            hasError = true
        }

        val updated = this.copy(
            occupationError = occErr,
            incomeError = incErr,
            educationError = eduErr,
            areaError = arErr
        )

        return Pair(!hasError, updated)
    }

    fun validateStep3(): Pair<Boolean, RegistrationFormData> {
        var hasError = false
        var passErr: String? = null
        var confErr: String? = null

        if (!isPasswordStrong(password)) {
            passErr = "Password must meet all 4 criteria below"
            hasError = true
        }

        if (confirmPassword.isEmpty()) {
            confErr = "Please confirm your password"
            hasError = true
        } else if (password != confirmPassword) {
            confErr = "Passwords do not match"
            hasError = true
        }

        val updated = this.copy(
            passwordError = passErr,
            confirmPasswordError = confErr
        )

        return Pair(!hasError, updated)
    }

    fun toRegisterRequest(): RegisterRequest {
        val calcAge = calculateAge(dateOfBirth)
        val numericIncome = RegistrationOptions.mapIncomeRangeToNumber(annualIncomeRange)
        return RegisterRequest(
            name = fullName.trim(),
            mobile = mobileNumber.trim(),
            email = email.trim().ifEmpty { null },
            password = password,
            age = if (calcAge in 18..120) calcAge else 25,
            gender = gender.trim(),
            state = state.trim(),
            district = district.trim(),
            verificationDocType = "Self-Declared",
            verificationDocUrl = "self_declaration_v1",
            scCategory = category.trim().ifEmpty { null },
            annualFamilyIncome = if (numericIncome > 0) numericIncome else null,
            occupation = occupation.trim().ifEmpty { null },
            businessType = null,
            businessRegistrationStatus = null,
            purpose = "Government Schemes & Welfare Support",
            requestedAmount = null,
            projectCost = null,
            educationLevel = educationLevel.trim().ifEmpty { null },
            areaType = areaType.trim().ifEmpty { null },
            dob = dateOfBirth.trim().ifEmpty { null }
        )
    }
}
