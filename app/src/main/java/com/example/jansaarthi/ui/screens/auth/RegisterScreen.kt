package com.example.jansaarthi.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jansaarthi.R
import com.example.jansaarthi.data.model.IndiaLocationData
import com.example.jansaarthi.data.model.RegistrationOptions
import com.example.jansaarthi.ui.theme.AppLogo
import com.example.jansaarthi.ui.theme.isAppInDarkTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit = onNavigateToLogin,
    viewModel: AuthViewModel = viewModel()
) {
    val isDark = isAppInDarkTheme()

    // Brand Design Tokens
    val bgColor = if (isDark) Color(0xFF0B1220) else Color(0xFFFAF8F2)
    val cardBgColor = if (isDark) Color(0xFF121B2D) else Color(0xFFFFFFFF)
    val navyColor = if (isDark) Color(0xFFF5F7FA) else Color(0xFF17243A)
    val goldPrimary = Color(0xFFC89124)
    val goldAccent = Color(0xFFD6A13A)
    val secondaryTextColor = if (isDark) Color(0xFFAAB4C3) else Color(0xFF657080)
    val dividerColor = if (isDark) Color(0xFF344155) else Color(0xFFE7E0D2)
    val errorColor = if (isDark) Color(0xFFFF6B6B) else Color(0xFFC62828)

    // Form state shared across all steps
    var formData by remember { mutableStateOf(RegistrationFormData()) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Listen for successful registration
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            viewModel.resetState()
            onNavigateToHome()
        }
    }

    // System Back Handler: Navigates backwards step by step without losing state
    BackHandler {
        when (formData.step) {
            RegistrationStep.INTRO -> onNavigateToLogin()
            RegistrationStep.BASIC_DETAILS -> formData = formData.copy(step = RegistrationStep.INTRO)
            RegistrationStep.ADDITIONAL_DETAILS -> formData = formData.copy(step = RegistrationStep.BASIC_DETAILS)
            RegistrationStep.CREATE_PASSWORD -> formData = formData.copy(step = RegistrationStep.ADDITIONAL_DETAILS)
        }
    }

    // Terms & Conditions Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Text(
                    text = "Terms & Privacy Policy",
                    fontWeight = FontWeight.Bold,
                    color = navyColor,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "JANSAARTHI Terms of Service & Privacy Commitment\n\n" +
                                "1. Purpose of Service: JANSAARTHI helps citizens discover, evaluate, and navigate government welfare schemes, subsidies, and credit assistance programs.\n\n" +
                                "2. Data Protection: Personal details such as your name, contact information, category, and income are used strictly for matching eligibility with official welfare guidelines.\n\n" +
                                "3. Security: All sensitive information is encrypted and transmitted securely. We never sell or share citizen data with unauthorized third parties.\n\n" +
                                "4. User Consent: By registering, you confirm that the information provided is accurate and consent to its use for scheme identification.",
                        color = secondaryTextColor,
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Understood", color = goldPrimary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = cardBgColor,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Material 3 Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = 852076800000L // Default approx year 1997
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        formData = formData.copy(
                            dateOfBirth = selectedDate.format(formatter),
                            dobError = null
                        )
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = goldPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = secondaryTextColor)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (formData.step == RegistrationStep.INTRO) {
        RegistrationIntroScreen(
            onNavigateToLogin = onNavigateToLogin,
            onGetStarted = {
                formData = formData.copy(step = RegistrationStep.BASIC_DETAILS)
            },
            navyColor = navyColor,
            secondaryTextColor = secondaryTextColor,
            goldPrimary = goldPrimary,
            goldAccent = goldAccent,
            cardBgColor = cardBgColor,
            dividerColor = dividerColor,
            bgColor = bgColor,
            isDark = isDark
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
        ) {
            // Decorative Background Layer
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawTopLeftGoldenCurves(goldPrimary, goldAccent, isDark)
                drawTricolourWave(isDark)
                drawBottomGoldenWaves(goldPrimary, goldAccent, isDark)
            }

            // Landmark Silhouettes
            Image(
                painter = painterResource(id = R.drawable.ic_indian_landmarks_silhouette),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 30.dp),
                colorFilter = ColorFilter.tint(
                    if (isDark) goldAccent.copy(alpha = 0.16f) else goldPrimary.copy(alpha = 0.24f)
                ),
                contentScale = ContentScale.FillWidth
            )

            // Main Scrollable Registration Flow
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                when (formData.step) {
                    RegistrationStep.INTRO -> {
                        // Handled in dedicated RegistrationIntroScreen above
                    }

                    // ==========================================
                    // SCREEN 2: STEP 1 — BASIC DETAILS
                    // ==========================================
                    RegistrationStep.BASIC_DETAILS -> {
                    RegistrationStepHeader(
                        currentStep = 1,
                        onBackClick = { formData = formData.copy(step = RegistrationStep.INTRO) },
                        navyColor = navyColor,
                        goldPrimary = goldPrimary,
                        secondaryTextColor = secondaryTextColor,
                        dividerColor = dividerColor,
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                        border = BorderStroke(1.dp, dividerColor.copy(alpha = if (isDark) 0.65f else 0.85f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            CardSectionHeader(
                                icon = Icons.Default.Person,
                                title = "Basic Details",
                                subtitle = "Provide your personal information to create an account",
                                goldPrimary = goldPrimary,
                                navyColor = navyColor,
                                secondaryTextColor = secondaryTextColor
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // 1. Full Name
                            RegistrationTextField(
                                value = formData.fullName,
                                onValueChange = {
                                    formData = formData.copy(fullName = it, fullNameError = null)
                                },
                                label = "Full Name *",
                                placeholder = "Enter your full name",
                                leadingIcon = Icons.Default.Person,
                                isError = formData.fullNameError != null,
                                errorMessage = formData.fullNameError,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                isDark = isDark
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 2. Mobile & Email 2-column layout
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    RegistrationTextField(
                                        value = formData.mobileNumber,
                                        onValueChange = {
                                            if (it.length <= 10 && it.all { ch -> ch.isDigit() }) {
                                                formData = formData.copy(mobileNumber = it, mobileError = null)
                                            }
                                        },
                                        label = "Mobile Number *",
                                        placeholder = "Enter mobile number",
                                        leadingIcon = Icons.Default.Phone,
                                        isError = formData.mobileError != null,
                                        errorMessage = formData.mobileError,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Phone,
                                            imeAction = ImeAction.Next
                                        ),
                                        isDark = isDark
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    RegistrationTextField(
                                        value = formData.email,
                                        onValueChange = {
                                            formData = formData.copy(email = it, emailError = null)
                                        },
                                        label = "Email Address",
                                        placeholder = "Enter email address",
                                        leadingIcon = Icons.Default.Email,
                                        isError = formData.emailError != null,
                                        errorMessage = formData.emailError,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Email,
                                            imeAction = ImeAction.Next
                                        ),
                                        isDark = isDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 3. Date of Birth & Gender 2-column layout
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    RegistrationClickableField(
                                        value = formData.dateOfBirth,
                                        onClick = { showDatePicker = true },
                                        label = "Date of Birth *",
                                        placeholder = "Select date of birth",
                                        leadingIcon = Icons.Default.DateRange,
                                        isError = formData.dobError != null,
                                        errorMessage = formData.dobError,
                                        isDark = isDark
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    RegistrationDropdownField(
                                        value = formData.gender,
                                        onValueChange = {
                                            formData = formData.copy(gender = it, genderError = null)
                                        },
                                        label = "Gender *",
                                        placeholder = "Select gender",
                                        options = RegistrationOptions.genders,
                                        leadingIcon = Icons.Default.Person,
                                        isError = formData.genderError != null,
                                        errorMessage = formData.genderError,
                                        isDark = isDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 4. State & District 2-column layout
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    RegistrationDropdownField(
                                        value = formData.state,
                                        onValueChange = { selectedState ->
                                            formData = formData.copy(
                                                state = selectedState,
                                                district = "", // Reset district on state change
                                                stateError = null,
                                                districtError = null
                                            )
                                        },
                                        label = "State *",
                                        placeholder = "Select state",
                                        options = IndiaLocationData.states,
                                        leadingIcon = Icons.Default.LocationOn,
                                        isError = formData.stateError != null,
                                        errorMessage = formData.stateError,
                                        isDark = isDark
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    val availableDistricts = IndiaLocationData.getDistrictsForState(formData.state)
                                    RegistrationDropdownField(
                                        value = formData.district,
                                        onValueChange = {
                                            formData = formData.copy(district = it, districtError = null)
                                        },
                                        label = "District *",
                                        placeholder = if (formData.state.isEmpty()) "Select state first" else "Select district",
                                        options = availableDistricts,
                                        leadingIcon = Icons.Default.LocationCity,
                                        isError = formData.districtError != null,
                                        errorMessage = formData.districtError,
                                        isDark = isDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 5. Category (full width)
                            RegistrationDropdownField(
                                value = formData.category,
                                onValueChange = {
                                    formData = formData.copy(category = it, categoryError = null)
                                },
                                label = "Category *",
                                placeholder = "Select category",
                                options = RegistrationOptions.categories,
                                leadingIcon = Icons.Default.GridView,
                                isError = formData.categoryError != null,
                                errorMessage = formData.categoryError,
                                isDark = isDark
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 6. Terms & Conditions Checkbox
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = formData.termsAccepted,
                                    onCheckedChange = {
                                        formData = formData.copy(termsAccepted = it, termsError = null)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = goldPrimary,
                                        checkmarkColor = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                val termsText = buildAnnotatedString {
                                    append("I agree to the ")
                                    pushStringAnnotation(tag = "TERMS", annotation = "terms")
                                    withStyle(style = SpanStyle(color = goldPrimary, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
                                        append("Terms & Conditions")
                                    }
                                    pop()
                                    append(" and ")
                                    pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
                                    withStyle(style = SpanStyle(color = goldPrimary, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
                                        append("Privacy Policy")
                                    }
                                    pop()
                                }
                                Text(
                                    text = termsText,
                                    fontSize = 12.5.sp,
                                    color = secondaryTextColor,
                                    modifier = Modifier.clickable { showTermsDialog = true }
                                )
                            }
                            if (formData.termsError != null) {
                                Text(
                                    text = formData.termsError!!,
                                    color = errorColor,
                                    fontSize = 11.5.sp,
                                    modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Golden Next Button
                            Button(
                                onClick = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    val (isValid, updated) = formData.validateStep1()
                                    formData = updated
                                    if (isValid) {
                                        formData = formData.copy(step = RegistrationStep.ADDITIONAL_DETAILS)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(goldPrimary, goldAccent)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Next",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }

                // ==========================================
                // SCREEN 3: STEP 2 — ADDITIONAL DETAILS
                // ==========================================
                RegistrationStep.ADDITIONAL_DETAILS -> {
                    RegistrationStepHeader(
                        currentStep = 2,
                        onBackClick = { formData = formData.copy(step = RegistrationStep.BASIC_DETAILS) },
                        navyColor = navyColor,
                        goldPrimary = goldPrimary,
                        secondaryTextColor = secondaryTextColor,
                        dividerColor = dividerColor,
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                        border = BorderStroke(1.dp, dividerColor.copy(alpha = if (isDark) 0.65f else 0.85f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            CardSectionHeader(
                                icon = Icons.Default.Work,
                                title = "Additional Details",
                                subtitle = "Help us understand your profile better",
                                goldPrimary = goldPrimary,
                                navyColor = navyColor,
                                secondaryTextColor = secondaryTextColor
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // 1. Occupation
                            RegistrationDropdownField(
                                value = formData.occupation,
                                onValueChange = {
                                    formData = formData.copy(occupation = it, occupationError = null)
                                },
                                label = "Occupation *",
                                placeholder = "Select occupation",
                                options = RegistrationOptions.occupations,
                                leadingIcon = Icons.Default.Work,
                                isError = formData.occupationError != null,
                                errorMessage = formData.occupationError,
                                isDark = isDark
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 2. Annual Income (Approx.)
                            RegistrationDropdownField(
                                value = formData.annualIncomeRange,
                                onValueChange = {
                                    formData = formData.copy(annualIncomeRange = it, incomeError = null)
                                },
                                label = "Annual Income (Approx.) *",
                                placeholder = "Select income range",
                                options = RegistrationOptions.incomeRanges,
                                leadingIcon = null,
                                leadingText = "₹",
                                isError = formData.incomeError != null,
                                errorMessage = formData.incomeError,
                                isDark = isDark
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 3. Education Level
                            RegistrationDropdownField(
                                value = formData.educationLevel,
                                onValueChange = {
                                    formData = formData.copy(educationLevel = it, educationError = null)
                                },
                                label = "Education Level *",
                                placeholder = "Select education level",
                                options = RegistrationOptions.educationLevels,
                                leadingIcon = Icons.Default.School,
                                isError = formData.educationError != null,
                                errorMessage = formData.educationError,
                                isDark = isDark
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 4. Area Type (Urban / Rural selectable cards)
                            Text(
                                text = "Area Type *",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = navyColor
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                RegistrationAreaTypeCard(
                                    label = "Urban",
                                    isSelected = formData.areaType == "Urban",
                                    onClick = { formData = formData.copy(areaType = "Urban", areaError = null) },
                                    modifier = Modifier.weight(1f),
                                    goldPrimary = goldPrimary,
                                    navyColor = navyColor,
                                    dividerColor = dividerColor,
                                    isDark = isDark
                                )

                                RegistrationAreaTypeCard(
                                    label = "Rural",
                                    isSelected = formData.areaType == "Rural",
                                    onClick = { formData = formData.copy(areaType = "Rural", areaError = null) },
                                    modifier = Modifier.weight(1f),
                                    goldPrimary = goldPrimary,
                                    navyColor = navyColor,
                                    dividerColor = dividerColor,
                                    isDark = isDark
                                )
                            }
                            if (formData.areaError != null) {
                                Text(
                                    text = formData.areaError!!,
                                    color = errorColor,
                                    fontSize = 11.5.sp,
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(22.dp))

                            // Navigation Buttons Row: [< Back] [Next ->]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        formData = formData.copy(step = RegistrationStep.BASIC_DETAILS)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, dividerColor),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = navyColor)
                                ) {
                                    Text(
                                        text = "Back",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = navyColor
                                    )
                                }

                                Button(
                                    onClick = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        val (isValid, updated) = formData.validateStep2()
                                        formData = updated
                                        if (isValid) {
                                            formData = formData.copy(step = RegistrationStep.CREATE_PASSWORD)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    colors = listOf(goldPrimary, goldAccent)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "Next",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }

                // ==========================================
                // SCREEN 4: STEP 3 — CREATE PASSWORD
                // ==========================================
                RegistrationStep.CREATE_PASSWORD -> {
                    RegistrationStepHeader(
                        currentStep = 3,
                        onBackClick = { formData = formData.copy(step = RegistrationStep.ADDITIONAL_DETAILS) },
                        navyColor = navyColor,
                        goldPrimary = goldPrimary,
                        secondaryTextColor = secondaryTextColor,
                        dividerColor = dividerColor,
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                        border = BorderStroke(1.dp, dividerColor.copy(alpha = if (isDark) 0.65f else 0.85f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            CardSectionHeader(
                                icon = Icons.Default.Lock,
                                title = "Create Password",
                                subtitle = "Set a strong password to secure your account",
                                goldPrimary = goldPrimary,
                                navyColor = navyColor,
                                secondaryTextColor = secondaryTextColor
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // 1. Password Field
                            RegistrationTextField(
                                value = formData.password,
                                onValueChange = {
                                    formData = formData.copy(password = it, passwordError = null)
                                },
                                label = "Password *",
                                placeholder = "Enter password",
                                leadingIcon = Icons.Default.Lock,
                                isPassword = true,
                                passwordVisible = formData.passwordVisible,
                                onTogglePasswordVisibility = {
                                    formData = formData.copy(passwordVisible = !formData.passwordVisible)
                                },
                                isError = formData.passwordError != null,
                                errorMessage = formData.passwordError,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Next
                                ),
                                isDark = isDark
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 2. Confirm Password Field
                            RegistrationTextField(
                                value = formData.confirmPassword,
                                onValueChange = {
                                    formData = formData.copy(confirmPassword = it, confirmPasswordError = null)
                                },
                                label = "Confirm Password *",
                                placeholder = "Re-enter password",
                                leadingIcon = Icons.Default.Lock,
                                isPassword = true,
                                passwordVisible = formData.confirmPasswordVisible,
                                onTogglePasswordVisibility = {
                                    formData = formData.copy(confirmPasswordVisible = !formData.confirmPasswordVisible)
                                },
                                isError = formData.confirmPasswordError != null,
                                errorMessage = formData.confirmPasswordError,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                isDark = isDark
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // 3. Live Password Requirements Box
                            LivePasswordRequirementsBox(
                                password = formData.password,
                                isDark = isDark
                            )

                            // Error display from ViewModel
                            if (authState is AuthState.Error) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = (authState as AuthState.Error).message,
                                    color = errorColor,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Navigation Buttons Row: [< Back] [Create Account ✓]
                            val isPasswordRulesSatisfied = RegistrationFormData.isPasswordStrong(formData.password)
                            val doPasswordsMatch = formData.password.isNotEmpty() && formData.password == formData.confirmPassword
                            val isSubmitEnabled = isPasswordRulesSatisfied && doPasswordsMatch && !isLoading

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        formData = formData.copy(step = RegistrationStep.ADDITIONAL_DETAILS)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, dividerColor),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = navyColor)
                                ) {
                                    Text(
                                        text = "Back",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = navyColor
                                    )
                                }

                                Button(
                                    onClick = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        val (isValid, updated) = formData.validateStep3()
                                        formData = updated
                                        if (isValid && !isLoading) {
                                            viewModel.register(formData.toRegisterRequest())
                                        }
                                    },
                                    enabled = isSubmitEnabled,
                                    modifier = Modifier
                                        .weight(1.8f)
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        disabledContainerColor = dividerColor.copy(alpha = 0.5f)
                                    ),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                brush = if (isSubmitEnabled) {
                                                    Brush.horizontalGradient(colors = listOf(goldPrimary, goldAccent))
                                                } else {
                                                    Brush.horizontalGradient(
                                                        colors = listOf(
                                                            dividerColor.copy(alpha = 0.6f),
                                                            dividerColor.copy(alpha = 0.6f)
                                                        )
                                                    )
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(
                                                color = Color.White,
                                                modifier = Modifier.size(22.dp),
                                                strokeWidth = 2.5.dp
                                            )
                                        } else {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "Create Account",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSubmitEnabled) Color.White else secondaryTextColor
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = null,
                                                    tint = if (isSubmitEnabled) Color.White else secondaryTextColor,
                                                    modifier = Modifier.size(17.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
    }
}

/**
 * Top header with Back arrow, Compact Brand name and Tagline, and 3-Step Stepper.
 */
@Composable
private fun RegistrationStepHeader(
    currentStep: Int,
    onBackClick: () -> Unit,
    navyColor: Color,
    goldPrimary: Color,
    secondaryTextColor: Color,
    dividerColor: Color,
    isDark: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top compact row with Back button and JANSAARTHI logo + text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = navyColor
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(28.dp)) {
                    AppLogo(modifier = Modifier.fillMaxSize())
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "JANSAARTHI",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = navyColor,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Aapki Zarurat. Sahi Sarkari Raasta.",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(48.dp)) // Balance the back button
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Create New Account",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = navyColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = "Register to access government schemes and services",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Normal,
            color = secondaryTextColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3-Step Horizontal Stepper
        RegistrationStepper(
            currentStep = currentStep,
            goldPrimary = goldPrimary,
            dividerColor = dividerColor,
            secondaryTextColor = secondaryTextColor,
            isDark = isDark
        )
    }
}

/**
 * 3-Step Horizontal Stepper matching the design reference:
 * - Completed step: Gold circle with white checkmark
 * - Active step: Gold circle with white number
 * - Inactive step: Grey circle with grey number
 */
@Composable
private fun RegistrationStepper(
    currentStep: Int,
    goldPrimary: Color,
    dividerColor: Color,
    secondaryTextColor: Color,
    isDark: Boolean
) {
    val steps = listOf(
        Pair(1, "Basic Details"),
        Pair(2, "Additional Details"),
        Pair(3, "Create Password")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            steps.forEachIndexed { index, (stepNumber, _) ->
                // Step Circle
                val isCompleted = stepNumber < currentStep
                val isActive = stepNumber == currentStep

                val circleBg = if (isCompleted || isActive) goldPrimary else if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                val textColor = if (isCompleted || isActive) Color.White else if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(circleBg),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = stepNumber.toString(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }

                // Connecting Line between steps
                if (index < steps.size - 1) {
                    val nextStepNumber = steps[index + 1].first
                    val isLineGold = currentStep >= nextStepNumber
                    val lineColor = if (isLineGold) goldPrimary else dividerColor

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .padding(horizontal = 6.dp)
                            .background(lineColor)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Step Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEach { (stepNumber, title) ->
                val isActive = stepNumber == currentStep
                val labelColor = if (isActive) goldPrimary else secondaryTextColor
                val labelWeight = if (isActive) FontWeight.Bold else FontWeight.Medium

                Text(
                    text = title,
                    fontSize = 11.5.sp,
                    fontWeight = labelWeight,
                    color = labelColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(100.dp)
                )
            }
        }
    }
}

/**
 * Registration Introduction Screen (Screen 1 of 4)
 * Featuring dedicated background-foreground layering, responsive vertical scrolling,
 * solid elevated golden CTA button, clear high-contrast Login link, and reserved
 * bottom spacing ensuring artwork never obscures interactive elements.
 */
@Composable
private fun RegistrationIntroScreen(
    onNavigateToLogin: () -> Unit,
    onGetStarted: () -> Unit,
    navyColor: Color,
    secondaryTextColor: Color,
    goldPrimary: Color,
    goldAccent: Color,
    cardBgColor: Color,
    dividerColor: Color,
    bgColor: Color,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Decorative Background Layer (strictly behind content)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawTopLeftGoldenCurves(goldPrimary, goldAccent, isDark)
            drawTricolourWave(isDark)
            drawIntroBottomGoldenWaves(goldPrimary, goldAccent, isDark)
        }

        // Indian Landmark Silhouettes with restrained, subtle opacity
        Image(
            painter = painterResource(id = R.drawable.ic_indian_landmarks_silhouette),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(105.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            colorFilter = ColorFilter.tint(
                if (isDark) goldAccent.copy(alpha = 0.06f) else goldPrimary.copy(alpha = 0.09f)
            ),
            contentScale = ContentScale.FillWidth
        )

        // Main Foreground Scrollable Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Login",
                        tint = navyColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 2. JANSAARTHI logo and branding
            Box(
                modifier = Modifier.size(66.dp),
                contentAlignment = Alignment.Center
            ) {
                AppLogo(modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "JANSAARTHI",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = navyColor,
                letterSpacing = 1.6.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Aapki Zarurat. Sahi Sarkari Raasta.",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = goldPrimary,
                letterSpacing = 0.2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Create New Account heading
            Text(
                text = "Create New Account",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = navyColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(3.dp))

            // 4. Registration description
            Text(
                text = "Register to access government schemes and services",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = secondaryTextColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Create your account information card
            IntroFeatureBadge(
                icon = Icons.Outlined.Person,
                title = "Create your account",
                subtitle = "Provide your basic and additional details",
                cardBgColor = cardBgColor,
                navyColor = navyColor,
                secondaryTextColor = secondaryTextColor,
                goldPrimary = goldPrimary,
                dividerColor = dividerColor,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 6. Secure & Verified information card
            IntroFeatureBadge(
                icon = Icons.Outlined.Shield,
                title = "Secure & Verified",
                subtitle = "Your information is safe with us",
                cardBgColor = cardBgColor,
                navyColor = navyColor,
                secondaryTextColor = secondaryTextColor,
                goldPrimary = goldPrimary,
                dividerColor = dividerColor,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 7. Get Personalized Support information card
            IntroFeatureBadge(
                icon = Icons.Outlined.FlashOn,
                title = "Get Personalized Support",
                subtitle = "Discover schemes and financial assistance for your needs",
                cardBgColor = cardBgColor,
                navyColor = navyColor,
                secondaryTextColor = secondaryTextColor,
                goldPrimary = goldPrimary,
                dividerColor = dividerColor,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 8. Get Started button with solid golden background and elevation
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = goldPrimary,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 3.dp,
                    pressedElevation = 6.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Get Started",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 9. Already have an account? Login
            Row(
                modifier = Modifier
                    .clickable { onNavigateToLogin() }
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = secondaryTextColor
                )
                Text(
                    text = "Login",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = goldPrimary,
                    textDecoration = TextDecoration.Underline
                )
            }

            // Reserved bottom space ensuring content is never obscured by bottom decorative waves
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

/**
 * Draws flowing golden layered waves along the bottom edge for the Intro screen.
 * Uses restrained opacity and proportional height so that decorative artwork
 * serves as an elegant background texture that never obscures foreground text or buttons.
 */
private fun DrawScope.drawIntroBottomGoldenWaves(
    goldPrimary: Color,
    goldAccent: Color,
    isDark: Boolean
) {
    val w = size.width
    val h = size.height

    // Layer 1: Back soft golden wave
    val wave1 = Path().apply {
        moveTo(0f, h - 60.dp.toPx())
        cubicTo(
            w * 0.35f, h - 75.dp.toPx(),
            w * 0.65f, h - 50.dp.toPx(),
            w, h - 65.dp.toPx()
        )
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = wave1,
        brush = Brush.verticalGradient(
            colors = listOf(
                goldAccent.copy(alpha = if (isDark) 0.08f else 0.14f),
                goldPrimary.copy(alpha = if (isDark) 0.14f else 0.22f)
            ),
            startY = h - 75.dp.toPx(),
            endY = h
        )
    )

    // Layer 2: Mid golden wave
    val wave2 = Path().apply {
        moveTo(0f, h - 42.dp.toPx())
        cubicTo(
            w * 0.28f, h - 35.dp.toPx(),
            w * 0.72f, h - 52.dp.toPx(),
            w, h - 36.dp.toPx()
        )
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = wave2,
        brush = Brush.verticalGradient(
            colors = listOf(
                goldAccent.copy(alpha = if (isDark) 0.14f else 0.22f),
                goldPrimary.copy(alpha = if (isDark) 0.20f else 0.30f)
            ),
            startY = h - 52.dp.toPx(),
            endY = h
        )
    )

    // Layer 3: Foreground golden wave
    val wave3 = Path().apply {
        moveTo(0f, h - 25.dp.toPx())
        cubicTo(
            w * 0.38f, h - 38.dp.toPx(),
            w * 0.75f, h - 18.dp.toPx(),
            w, h - 8.dp.toPx()
        )
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = wave3,
        brush = Brush.verticalGradient(
            colors = listOf(
                goldAccent.copy(alpha = if (isDark) 0.18f else 0.28f),
                goldPrimary.copy(alpha = if (isDark) 0.25f else 0.38f)
            ),
            startY = h - 38.dp.toPx(),
            endY = h
        )
    )
}

/**
 * Feature Badge used on the Registration Introduction Screen.
 */
@Composable
private fun IntroFeatureBadge(
    icon: ImageVector,
    title: String,
    subtitle: String,
    cardBgColor: Color,
    navyColor: Color,
    secondaryTextColor: Color,
    goldPrimary: Color,
    dividerColor: Color,
    isDark: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(1.dp, dividerColor.copy(alpha = if (isDark) 0.65f else 0.85f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(goldPrimary.copy(alpha = if (isDark) 0.22f else 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = goldPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = navyColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.5.sp,
                    color = secondaryTextColor,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

/**
 * Card Section Header with golden square icon and title/subtitle.
 */
@Composable
private fun CardSectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String,
    goldPrimary: Color,
    navyColor: Color,
    secondaryTextColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(goldPrimary.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = goldPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = navyColor
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = secondaryTextColor
            )
        }
    }
}

/**
 * Reusable Styled Text Field for the registration form.
 */
@Composable
private fun RegistrationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector? = null,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isDark: Boolean
) {
    val navyColor = if (isDark) Color(0xFFF5F7FA) else Color(0xFF17243A)
    val inputBgColor = if (isDark) Color(0xFF17243A) else Color(0xFFFFFFFF)
    val goldPrimary = Color(0xFFC89124)
    val secondaryTextColor = if (isDark) Color(0xFFAAB4C3) else Color(0xFF657080)
    val dividerColor = if (isDark) Color(0xFF344155) else Color(0xFFE7E0D2)
    val errorColor = if (isDark) Color(0xFFFF6B6B) else Color(0xFFC62828)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isError) errorColor else navyColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(text = placeholder, fontSize = 13.sp, color = secondaryTextColor.copy(alpha = 0.65f))
            },
            leadingIcon = leadingIcon?.let { icon ->
                {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isError) errorColor else secondaryTextColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            trailingIcon = if (isPassword && onTogglePasswordVisibility != null) {
                {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = secondaryTextColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = keyboardOptions,
            singleLine = true,
            isError = isError,
            textStyle = TextStyle(
                fontSize = 13.5.sp,
                color = navyColor,
                fontWeight = FontWeight.Medium
            ),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = inputBgColor,
                unfocusedContainerColor = inputBgColor,
                errorContainerColor = inputBgColor,
                focusedBorderColor = goldPrimary,
                unfocusedBorderColor = dividerColor,
                errorBorderColor = errorColor,
                cursorColor = goldPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = errorColor,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

/**
 * Clickable read-only field (e.g. for Date of Birth picker).
 */
@Composable
private fun RegistrationClickableField(
    value: String,
    onClick: () -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    isError: Boolean = false,
    errorMessage: String? = null,
    isDark: Boolean
) {
    val navyColor = if (isDark) Color(0xFFF5F7FA) else Color(0xFF17243A)
    val inputBgColor = if (isDark) Color(0xFF17243A) else Color(0xFFFFFFFF)
    val secondaryTextColor = if (isDark) Color(0xFFAAB4C3) else Color(0xFF657080)
    val dividerColor = if (isDark) Color(0xFF344155) else Color(0xFFE7E0D2)
    val errorColor = if (isDark) Color(0xFFFF6B6B) else Color(0xFFC62828)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isError) errorColor else navyColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(inputBgColor)
                .border(
                    width = 1.dp,
                    color = if (isError) errorColor else dividerColor,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable { onClick() }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (isError) errorColor else secondaryTextColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                if (value.isNotEmpty()) {
                    Text(
                        text = value,
                        fontSize = 13.5.sp,
                        color = navyColor,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = placeholder,
                        fontSize = 13.sp,
                        color = secondaryTextColor.copy(alpha = 0.65f)
                    )
                }
            }
        }
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = errorColor,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

/**
 * Material 3 Exposed Dropdown Menu field.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegistrationDropdownField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    options: List<String>,
    leadingIcon: ImageVector? = null,
    leadingText: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    isDark: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    val navyColor = if (isDark) Color(0xFFF5F7FA) else Color(0xFF17243A)
    val inputBgColor = if (isDark) Color(0xFF17243A) else Color(0xFFFFFFFF)
    val goldPrimary = Color(0xFFC89124)
    val secondaryTextColor = if (isDark) Color(0xFFAAB4C3) else Color(0xFF657080)
    val dividerColor = if (isDark) Color(0xFF344155) else Color(0xFFE7E0D2)
    val errorColor = if (isDark) Color(0xFFFF6B6B) else Color(0xFFC62828)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isError) errorColor else navyColor
        )
        Spacer(modifier = Modifier.height(4.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                placeholder = {
                    Text(text = placeholder, fontSize = 13.sp, color = secondaryTextColor.copy(alpha = 0.65f))
                },
                leadingIcon = {
                    if (leadingIcon != null) {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            tint = if (isError) errorColor else secondaryTextColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    } else if (leadingText != null) {
                        Text(
                            text = leadingText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = goldPrimary
                        )
                    }
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                isError = isError,
                textStyle = TextStyle(
                    fontSize = 13.5.sp,
                    color = navyColor,
                    fontWeight = FontWeight.Medium
                ),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = inputBgColor,
                    unfocusedContainerColor = inputBgColor,
                    errorContainerColor = inputBgColor,
                    focusedBorderColor = goldPrimary,
                    unfocusedBorderColor = dividerColor,
                    errorBorderColor = errorColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(inputBgColor)
                    .heightIn(max = 280.dp)
            ) {
                if (options.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No options available", fontSize = 13.sp, color = secondaryTextColor) },
                        onClick = { expanded = false }
                    )
                } else {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (option == value) FontWeight.Bold else FontWeight.Normal,
                                    color = if (option == value) goldPrimary else navyColor
                                )
                            },
                            onClick = {
                                onValueChange(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = errorColor,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

/**
 * Selectable card for Area Type (Urban / Rural).
 */
@Composable
private fun RegistrationAreaTypeCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    goldPrimary: Color,
    navyColor: Color,
    dividerColor: Color,
    isDark: Boolean
) {
    val cardBg = if (isSelected) {
        if (isDark) Color(0xFF262319) else Color(0xFFFEF9EE)
    } else {
        if (isDark) Color(0xFF17243A) else Color(0xFFFFFFFF)
    }

    val borderColor = if (isSelected) goldPrimary else dividerColor

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Radio circle indicator
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (isSelected) 5.dp else 1.5.dp,
                        color = if (isSelected) goldPrimary else dividerColor,
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = label,
                fontSize = 13.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) goldPrimary else navyColor
            )
        }
    }
}

/**
 * Live Password Requirements Box with active green checkmarks.
 */
@Composable
private fun LivePasswordRequirementsBox(
    password: String,
    isDark: Boolean
) {
    val hasMinLength = RegistrationFormData.isMinLengthMet(password)
    val hasUpperCase = RegistrationFormData.isUpperCaseMet(password)
    val hasLowerCase = RegistrationFormData.isLowerCaseMet(password)
    val hasDigit = RegistrationFormData.isDigitMet(password)

    val boxBg = if (isDark) Color(0xFF092015) else Color(0xFFF0FDF4)
    val boxBorder = if (isDark) Color(0xFF14532D) else Color(0xFFBBF7D0)
    val successColor = if (isDark) Color(0xFF4ADE80) else Color(0xFF16845B)
    val unfulfilledColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = boxBg),
        border = BorderStroke(1.dp, boxBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = "Password must have:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = successColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            PasswordRequirementRow(
                isMet = hasMinLength,
                text = "At least 8 characters",
                successColor = successColor,
                unfulfilledColor = unfulfilledColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            PasswordRequirementRow(
                isMet = hasUpperCase,
                text = "One uppercase letter (A–Z)",
                successColor = successColor,
                unfulfilledColor = unfulfilledColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            PasswordRequirementRow(
                isMet = hasLowerCase,
                text = "One lowercase letter (a–z)",
                successColor = successColor,
                unfulfilledColor = unfulfilledColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            PasswordRequirementRow(
                isMet = hasDigit,
                text = "One number (0–9)",
                successColor = successColor,
                unfulfilledColor = unfulfilledColor
            )
        }
    }
}

@Composable
private fun PasswordRequirementRow(
    isMet: Boolean,
    text: String,
    successColor: Color,
    unfulfilledColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isMet) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
            contentDescription = null,
            tint = if (isMet) successColor else unfulfilledColor,
            modifier = Modifier.size(15.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (isMet) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isMet) successColor else unfulfilledColor
        )
    }
}
