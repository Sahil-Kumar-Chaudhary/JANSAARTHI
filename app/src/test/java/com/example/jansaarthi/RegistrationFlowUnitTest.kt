package com.example.jansaarthi

import com.example.jansaarthi.data.model.IndiaLocationData
import com.example.jansaarthi.data.model.RegistrationOptions
import com.example.jansaarthi.ui.screens.auth.RegistrationFormData
import com.example.jansaarthi.ui.screens.auth.RegistrationStep
import org.junit.Assert.*
import org.junit.Test

class RegistrationFlowUnitTest {

    @Test
    fun testStep1Validation_EmptyFieldsFail() {
        val form = RegistrationFormData()
        val (isValid, updated) = form.validateStep1()

        assertFalse("Empty form must fail Step 1 validation", isValid)
        assertNotNull("Full name error must be set", updated.fullNameError)
        assertNotNull("Mobile number error must be set", updated.mobileError)
        assertNotNull("Date of birth error must be set", updated.dobError)
        assertNotNull("Gender error must be set", updated.genderError)
        assertNotNull("State error must be set", updated.stateError)
        assertNotNull("District error must be set", updated.districtError)
        assertNotNull("Category error must be set", updated.categoryError)
        assertNotNull("Terms error must be set", updated.termsError)
    }

    @Test
    fun testStep1Validation_InvalidMobileNumber() {
        val form = RegistrationFormData(
            fullName = "Sahil Kumar",
            mobileNumber = "12345", // Invalid length and start digit
            dateOfBirth = "15/08/1995",
            gender = "Male",
            state = "Delhi",
            district = "New Delhi",
            category = "General",
            termsAccepted = true
        )
        val (isValid, updated) = form.validateStep1()

        assertFalse(isValid)
        assertEquals("Enter a valid 10-digit Indian mobile number", updated.mobileError)
    }

    @Test
    fun testStep1Validation_ValidDataSucceeds() {
        val form = RegistrationFormData(
            fullName = "Ramesh Kumar Sharma",
            mobileNumber = "9876543210",
            email = "ramesh@example.com",
            dateOfBirth = "15/08/1995",
            gender = "Male",
            state = "Maharashtra",
            district = "Pune",
            category = "OBC",
            termsAccepted = true
        )
        val (isValid, updated) = form.validateStep1()

        assertTrue("Valid Step 1 data must pass validation", isValid)
        assertNull(updated.fullNameError)
        assertNull(updated.mobileError)
        assertNull(updated.emailError)
        assertNull(updated.dobError)
        assertNull(updated.genderError)
        assertNull(updated.stateError)
        assertNull(updated.districtError)
        assertNull(updated.categoryError)
        assertNull(updated.termsError)
    }

    @Test
    fun testAgeCalculation_UnderageApplicantRejected() {
        // Someone born in 2020 is only ~6 years old
        val calculatedAge = RegistrationFormData.calculateAge("01/01/2020")
        assertTrue("Calculated age should be under 18", calculatedAge < 18)

        val form = RegistrationFormData(
            fullName = "Child Applicant",
            mobileNumber = "9876543210",
            dateOfBirth = "01/01/2020",
            gender = "Male",
            state = "Delhi",
            district = "New Delhi",
            category = "General",
            termsAccepted = true
        )
        val (isValid, updated) = form.validateStep1()
        assertFalse(isValid)
        assertEquals("Applicant must be at least 18 years old", updated.dobError)
    }

    @Test
    fun testStep2Validation_EmptyFieldsFail() {
        val form = RegistrationFormData()
        val (isValid, updated) = form.validateStep2()

        assertFalse(isValid)
        assertNotNull(updated.occupationError)
        assertNotNull(updated.incomeError)
        assertNotNull(updated.educationError)
    }

    @Test
    fun testStep2Validation_ValidDataSucceeds() {
        val form = RegistrationFormData(
            occupation = "Self-Employed",
            annualIncomeRange = "₹1,00,000 – ₹3,00,000",
            educationLevel = "Graduate / Bachelor's",
            areaType = "Urban"
        )
        val (isValid, updated) = form.validateStep2()

        assertTrue(isValid)
        assertNull(updated.occupationError)
        assertNull(updated.incomeError)
        assertNull(updated.educationError)
        assertNull(updated.areaError)
    }

    @Test
    fun testStep3PasswordValidation_WeakPasswordRules() {
        // Test short password
        assertFalse(RegistrationFormData.isMinLengthMet("Ab1!"))
        // Test missing uppercase
        assertFalse(RegistrationFormData.isUpperCaseMet("password123"))
        // Test missing lowercase
        assertFalse(RegistrationFormData.isLowerCaseMet("PASSWORD123"))
        // Test missing digit
        assertFalse(RegistrationFormData.isDigitMet("PasswordOnly"))

        // Strong password meeting all 4 rules
        val strongPass = "SecurePass123"
        assertTrue(RegistrationFormData.isMinLengthMet(strongPass))
        assertTrue(RegistrationFormData.isUpperCaseMet(strongPass))
        assertTrue(RegistrationFormData.isLowerCaseMet(strongPass))
        assertTrue(RegistrationFormData.isDigitMet(strongPass))
        assertTrue(RegistrationFormData.isPasswordStrong(strongPass))
    }

    @Test
    fun testStep3PasswordValidation_PasswordMismatch() {
        val form = RegistrationFormData(
            password = "SecurePassword1",
            confirmPassword = "DifferentPassword2"
        )
        val (isValid, updated) = form.validateStep3()

        assertFalse(isValid)
        assertEquals("Passwords do not match", updated.confirmPasswordError)
    }

    @Test
    fun testStep3PasswordValidation_Success() {
        val form = RegistrationFormData(
            password = "StrongPassword99",
            confirmPassword = "StrongPassword99"
        )
        val (isValid, updated) = form.validateStep3()

        assertTrue(isValid)
        assertNull(updated.passwordError)
        assertNull(updated.confirmPasswordError)
    }

    @Test
    fun testStatePreservation_DataPersistsAcrossStepTransitions() {
        // Start in Step 1
        var form = RegistrationFormData(
            step = RegistrationStep.BASIC_DETAILS,
            fullName = "Ananya Patel",
            mobileNumber = "9123456789",
            email = "ananya.patel@example.com",
            dateOfBirth = "20/05/1996",
            gender = "Female",
            state = "Gujarat",
            district = "Ahmedabad",
            category = "General",
            termsAccepted = true
        )

        // Move to Step 2
        form = form.copy(
            step = RegistrationStep.ADDITIONAL_DETAILS,
            occupation = "Business Owner / Entrepreneur",
            annualIncomeRange = "₹3,00,000 – ₹5,00,000",
            educationLevel = "Post Graduate / Master's",
            areaType = "Urban"
        )

        // Move to Step 3
        form = form.copy(
            step = RegistrationStep.CREATE_PASSWORD,
            password = "MyStrongPassword1",
            confirmPassword = "MyStrongPassword1"
        )

        // Navigate backwards to Step 2
        form = form.copy(step = RegistrationStep.ADDITIONAL_DETAILS)
        assertEquals("Business Owner / Entrepreneur", form.occupation)
        assertEquals("₹3,00,000 – ₹5,00,000", form.annualIncomeRange)
        assertEquals("MyStrongPassword1", form.password)

        // Navigate backwards to Step 1
        form = form.copy(step = RegistrationStep.BASIC_DETAILS)
        assertEquals("Ananya Patel", form.fullName)
        assertEquals("9123456789", form.mobileNumber)
        assertEquals("ananya.patel@example.com", form.email)
        assertEquals("20/05/1996", form.dateOfBirth)
        assertEquals("Gujarat", form.state)
        assertEquals("Ahmedabad", form.district)
        assertEquals("General", form.category)
        assertTrue(form.termsAccepted)

        // Generate RegisterRequest from state
        val request = form.toRegisterRequest()
        assertEquals("Ananya Patel", request.name)
        assertEquals("9123456789", request.mobile)
        assertEquals("ananya.patel@example.com", request.email)
        assertEquals("MyStrongPassword1", request.password)
        assertEquals("Gujarat", request.state)
        assertEquals("Ahmedabad", request.district)
        assertEquals("General", request.scCategory)
        assertEquals(400000, request.annualFamilyIncome)
        assertEquals("Self-Declared", request.verificationDocType)
        assertEquals("self_declaration_v1", request.verificationDocUrl)
        assertEquals("Post Graduate / Master's", request.educationLevel)
        assertEquals("Urban", request.areaType)
        assertEquals("20/05/1996", request.dob)
    }

    @Test
    fun testIndiaLocationData_HasAuthenticStatesAndDistricts() {
        assertEquals("Must contain 36 States and Union Territories", 36, IndiaLocationData.states.size)
        assertTrue("Must include Uttar Pradesh", IndiaLocationData.states.contains("Uttar Pradesh"))
        assertTrue("Must include Maharashtra", IndiaLocationData.states.contains("Maharashtra"))
        assertTrue("Must include Delhi", IndiaLocationData.states.contains("Delhi"))

        val maharashtraDistricts = IndiaLocationData.getDistrictsForState("Maharashtra")
        assertTrue("Maharashtra districts should contain Pune", maharashtraDistricts.contains("Pune"))
        assertTrue("Maharashtra districts should contain Mumbai City", maharashtraDistricts.contains("Mumbai City"))

        val upDistricts = IndiaLocationData.getDistrictsForState("Uttar Pradesh")
        assertTrue("UP districts should contain Lucknow", upDistricts.contains("Lucknow"))
        assertTrue("UP districts should contain Varanasi", upDistricts.contains("Varanasi"))

        val delhiDistricts = IndiaLocationData.getDistrictsForState("Delhi")
        assertTrue("Delhi districts should contain New Delhi", delhiDistricts.contains("New Delhi"))
    }

    @Test
    fun testRegistrationOptions_IncomeMapping() {
        assertEquals(80000, RegistrationOptions.mapIncomeRangeToNumber("Below ₹1,00,000"))
        assertEquals(200000, RegistrationOptions.mapIncomeRangeToNumber("₹1,00,000 – ₹3,00,000"))
        assertEquals(400000, RegistrationOptions.mapIncomeRangeToNumber("₹3,00,000 – ₹5,00,000"))
        assertEquals(750000, RegistrationOptions.mapIncomeRangeToNumber("₹5,00,000 – ₹10,00,000"))
        assertEquals(1200000, RegistrationOptions.mapIncomeRangeToNumber("Above ₹10,00,000"))
        assertEquals(0, RegistrationOptions.mapIncomeRangeToNumber("Unknown"))
    }
}
