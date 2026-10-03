package com.example.jansaarthi

import com.example.jansaarthi.ui.screens.auth.ForgotPasswordState
import com.example.jansaarthi.ui.screens.auth.ForgotPasswordStep
import com.example.jansaarthi.ui.screens.auth.RegistrationFormData
import org.junit.Assert.*
import org.junit.Test

class ForgotPasswordUnitTest {

    @Test
    fun testInitialState_DefaultsToMobileInput() {
        val state = ForgotPasswordState()
        assertEquals(ForgotPasswordStep.MOBILE_INPUT, state.step)
        assertEquals("", state.mobile)
        assertEquals("", state.otp)
        assertEquals("", state.newPassword)
        assertEquals("", state.confirmPassword)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertNull(state.mobileError)
        assertNull(state.otpError)
        assertNull(state.passwordError)
        assertNull(state.confirmPasswordError)
    }

    @Test
    fun testMobileNumber_ValidationRules() {
        val emptyMobile = ""
        val shortMobile = "98765"
        val alphaMobile = "98765abcde"
        val validMobile = "9876543210"

        val regex = Regex("^[0-9]{10}$")

        assertFalse("Empty mobile must fail regex", emptyMobile.matches(regex))
        assertFalse("Short mobile must fail regex", shortMobile.matches(regex))
        assertFalse("Alphanumeric mobile must fail regex", alphaMobile.matches(regex))
        assertTrue("10-digit mobile must pass regex", validMobile.matches(regex))
    }

    @Test
    fun testOtp_ValidationRules() {
        val emptyOtp = ""
        val shortOtp = "12345"
        val longOtp = "1234567"
        val validOtp = "123456"

        assertFalse("Empty OTP must be invalid", emptyOtp.length == 6 && emptyOtp.all { it.isDigit() })
        assertFalse("Short OTP must be invalid", shortOtp.length == 6 && shortOtp.all { it.isDigit() })
        assertFalse("Long OTP must be invalid", longOtp.length == 6 && longOtp.all { it.isDigit() })
        assertTrue("6-digit numeric OTP must be valid", validOtp.length == 6 && validOtp.all { it.isDigit() })
    }

    @Test
    fun testPasswordRequirements_MinLength() {
        assertFalse(RegistrationFormData.isMinLengthMet("Ab1"))
        assertFalse(RegistrationFormData.isMinLengthMet("Abcdef7"))
        assertTrue(RegistrationFormData.isMinLengthMet("Abcdefg8"))
        assertTrue(RegistrationFormData.isMinLengthMet("VeryStrongPassword123!"))
    }

    @Test
    fun testPasswordRequirements_Uppercase() {
        assertFalse(RegistrationFormData.isUpperCaseMet("alllowercase123"))
        assertTrue(RegistrationFormData.isUpperCaseMet("WithUppercase123"))
    }

    @Test
    fun testPasswordRequirements_Lowercase() {
        assertFalse(RegistrationFormData.isLowerCaseMet("ALLLOWERCASE123"))
        assertTrue(RegistrationFormData.isLowerCaseMet("WithLowercase123"))
    }

    @Test
    fun testPasswordRequirements_Digit() {
        assertFalse(RegistrationFormData.isDigitMet("NoDigitsHereAtAll"))
        assertTrue(RegistrationFormData.isDigitMet("HasDigit123"))
    }

    @Test
    fun testPasswordStrength_CombinedCheck() {
        // Missing digit
        assertFalse(RegistrationFormData.isPasswordStrong("PasswordOnly"))
        // Missing uppercase
        assertFalse(RegistrationFormData.isPasswordStrong("password123"))
        // Missing lowercase
        assertFalse(RegistrationFormData.isPasswordStrong("PASSWORD123"))
        // Too short
        assertFalse(RegistrationFormData.isPasswordStrong("Pass1"))
        // Meets all criteria
        assertTrue(RegistrationFormData.isPasswordStrong("JanSaarthi@2026"))
        assertTrue(RegistrationFormData.isPasswordStrong("SecurePassword1"))
    }

    @Test
    fun testPasswordConfirmation_Matching() {
        val pass = "JanSaarthi@2026"
        val matchingConfirm = "JanSaarthi@2026"
        val nonMatchingConfirm = "DifferentPassword@2026"

        assertTrue("Passwords must match", pass == matchingConfirm)
        assertFalse("Mismatch must be detected", pass == nonMatchingConfirm)
    }

    @Test
    fun testStateProgression_StepTransitions() {
        var state = ForgotPasswordState()
        assertEquals(ForgotPasswordStep.MOBILE_INPUT, state.step)

        // Step 1 -> Step 2
        state = state.copy(
            mobile = "9876543210",
            step = ForgotPasswordStep.OTP_VERIFICATION,
            generatedOtp = "654321"
        )
        assertEquals(ForgotPasswordStep.OTP_VERIFICATION, state.step)
        assertEquals("654321", state.generatedOtp)

        // Step 2 -> Step 3
        state = state.copy(
            otp = "654321",
            step = ForgotPasswordStep.NEW_PASSWORD,
            resetToken = "valid_reset_jwt_token"
        )
        assertEquals(ForgotPasswordStep.NEW_PASSWORD, state.step)
        assertEquals("valid_reset_jwt_token", state.resetToken)

        // Step 3 -> Step 4 (Success)
        state = state.copy(
            newPassword = "NewPassword@2026",
            confirmPassword = "NewPassword@2026",
            step = ForgotPasswordStep.SUCCESS,
            successMessage = "Password reset successful!"
        )
        assertEquals(ForgotPasswordStep.SUCCESS, state.step)
        assertEquals("Password reset successful!", state.successMessage)
    }
}
