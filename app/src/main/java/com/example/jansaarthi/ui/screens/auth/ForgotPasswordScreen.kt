package com.example.jansaarthi.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jansaarthi.R
import com.example.jansaarthi.ui.theme.AppLogo
import com.example.jansaarthi.ui.theme.isAppInDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val isDark = isAppInDarkTheme()

    // Brand Design Tokens
    val bgColor = if (isDark) Color(0xFF0B1220) else Color(0xFFFAF8F2)
    val cardBgColor = if (isDark) Color(0xFF121B2D) else Color(0xFFFFFFFF)
    val inputBgColor = if (isDark) Color(0xFF17243A) else Color(0xFFFFFFFF)
    val navyColor = if (isDark) Color(0xFFF5F7FA) else Color(0xFF17243A)
    val goldPrimary = Color(0xFFC89124)
    val goldAccent = Color(0xFFD6A13A)
    val secondaryTextColor = if (isDark) Color(0xFFAAB4C3) else Color(0xFF657080)
    val dividerColor = if (isDark) Color(0xFF344155) else Color(0xFFE7E0D2)
    val errorColor = if (isDark) Color(0xFFFF6B6B) else Color(0xFFC62828)
    val successColor = if (isDark) Color(0xFF4ADE80) else Color(0xFF16845B)

    val state by viewModel.forgotPasswordState.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetForgotPasswordState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Decorative Layer
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawTopLeftGoldenCurves(goldPrimary, goldAccent, isDark)
            drawTricolourWave(isDark)
            drawBottomGoldenWaves(goldPrimary, goldAccent, isDark)
        }

        // Low-Opacity Landmark Silhouettes
        Image(
            painter = painterResource(id = R.drawable.ic_indian_landmarks_silhouette),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(130.dp)
                .padding(bottom = 30.dp),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                if (isDark) goldAccent.copy(alpha = 0.16f) else goldPrimary.copy(alpha = 0.24f)
            ),
            contentScale = ContentScale.FillWidth
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (state.step == ForgotPasswordStep.OTP_VERIFICATION) {
                            viewModel.resetForgotPasswordState()
                        } else {
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1EADB),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = navyColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Password Recovery",
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = navyColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Branding Header
            AppLogo(
                modifier = Modifier.size(52.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "JANSAARTHI",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = navyColor,
                letterSpacing = 0.8.sp
            )

            Text(
                text = "Aapki Zarurat. Sahi Sarkari Raasta.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = goldPrimary,
                letterSpacing = 0.2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            DecorativeGoldenDivider(
                goldColor = goldPrimary,
                accentColor = goldAccent
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Step Progress Indicator (Steps 1, 2, 3)
            if (state.step != ForgotPasswordStep.SUCCESS) {
                StepProgressHeader(
                    currentStep = state.step,
                    goldPrimary = goldPrimary,
                    navyColor = navyColor,
                    secondaryColor = secondaryTextColor,
                    dividerColor = dividerColor,
                    isDark = isDark
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Main Card Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(
                    width = 1.dp,
                    color = dividerColor.copy(alpha = if (isDark) 0.65f else 0.85f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Global Error Banner
                    if (state.errorMessage != null) {
                        Surface(
                            color = errorColor.copy(alpha = if (isDark) 0.18f else 0.10f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, errorColor.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                        ) {
                            Text(
                                text = state.errorMessage!!,
                                color = errorColor,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    when (state.step) {
                        ForgotPasswordStep.MOBILE_INPUT -> {
                            Step1MobileView(
                                state = state,
                                viewModel = viewModel,
                                navyColor = navyColor,
                                secondaryTextColor = secondaryTextColor,
                                inputBgColor = inputBgColor,
                                dividerColor = dividerColor,
                                goldPrimary = goldPrimary,
                                errorColor = errorColor,
                                onSubmit = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    viewModel.sendForgotOtp()
                                },
                                onNavigateToLogin = onNavigateToLogin
                            )
                        }
                        ForgotPasswordStep.OTP_VERIFICATION -> {
                            Step2OtpView(
                                state = state,
                                viewModel = viewModel,
                                navyColor = navyColor,
                                secondaryTextColor = secondaryTextColor,
                                inputBgColor = inputBgColor,
                                dividerColor = dividerColor,
                                goldPrimary = goldPrimary,
                                errorColor = errorColor,
                                successColor = successColor,
                                isDark = isDark,
                                onVerify = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    viewModel.verifyForgotOtp()
                                },
                                onResend = {
                                    viewModel.sendForgotOtp()
                                }
                            )
                        }
                        ForgotPasswordStep.NEW_PASSWORD -> {
                            Step3NewPasswordView(
                                state = state,
                                viewModel = viewModel,
                                navyColor = navyColor,
                                secondaryTextColor = secondaryTextColor,
                                inputBgColor = inputBgColor,
                                dividerColor = dividerColor,
                                goldPrimary = goldPrimary,
                                errorColor = errorColor,
                                successColor = successColor,
                                isDark = isDark,
                                onSubmit = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    viewModel.submitNewPassword()
                                }
                            )
                        }
                        ForgotPasswordStep.SUCCESS -> {
                            Step4SuccessView(
                                message = state.successMessage ?: "Password has been reset successfully.",
                                navyColor = navyColor,
                                secondaryTextColor = secondaryTextColor,
                                goldPrimary = goldPrimary,
                                successColor = successColor,
                                isDark = isDark,
                                onNavigateToLogin = onNavigateToLogin
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Step Progress Indicator showing 3 sequential circles with connecting lines.
 */
@Composable
private fun StepProgressHeader(
    currentStep: ForgotPasswordStep,
    goldPrimary: Color,
    navyColor: Color,
    secondaryColor: Color,
    dividerColor: Color,
    isDark: Boolean
) {
    val stepIndex = when (currentStep) {
        ForgotPasswordStep.MOBILE_INPUT -> 1
        ForgotPasswordStep.OTP_VERIFICATION -> 2
        ForgotPasswordStep.NEW_PASSWORD -> 3
        ForgotPasswordStep.SUCCESS -> 4
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StepCircle(
            number = 1,
            label = "Mobile",
            isActive = stepIndex == 1,
            isCompleted = stepIndex > 1,
            goldPrimary = goldPrimary,
            navyColor = navyColor,
            secondaryColor = secondaryColor,
            isDark = isDark
        )

        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp),
            color = if (stepIndex > 1) goldPrimary else dividerColor,
            thickness = 2.dp
        )

        StepCircle(
            number = 2,
            label = "Verify OTP",
            isActive = stepIndex == 2,
            isCompleted = stepIndex > 2,
            goldPrimary = goldPrimary,
            navyColor = navyColor,
            secondaryColor = secondaryColor,
            isDark = isDark
        )

        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp),
            color = if (stepIndex > 2) goldPrimary else dividerColor,
            thickness = 2.dp
        )

        StepCircle(
            number = 3,
            label = "New Password",
            isActive = stepIndex == 3,
            isCompleted = stepIndex > 3,
            goldPrimary = goldPrimary,
            navyColor = navyColor,
            secondaryColor = secondaryColor,
            isDark = isDark
        )
    }
}

@Composable
private fun StepCircle(
    number: Int,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean,
    goldPrimary: Color,
    navyColor: Color,
    secondaryColor: Color,
    isDark: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(
                    color = when {
                        isCompleted -> Color(0xFF16845B)
                        isActive -> goldPrimary
                        isDark -> Color(0xFF1E293B)
                        else -> Color(0xFFE2E8F0)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = number.toString(),
                    color = if (isActive) Color.White else secondaryColor,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isActive || isCompleted) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) goldPrimary else secondaryColor
        )
    }
}

/**
 * Step 1: Mobile Number Input
 */
@Composable
private fun Step1MobileView(
    state: ForgotPasswordState,
    viewModel: AuthViewModel,
    navyColor: Color,
    secondaryTextColor: Color,
    inputBgColor: Color,
    dividerColor: Color,
    goldPrimary: Color,
    errorColor: Color,
    onSubmit: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Text(
        text = "Forgot Password?",
        fontSize = 21.sp,
        fontWeight = FontWeight.ExtraBold,
        color = navyColor
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "Enter your 10-digit registered mobile number. We will send you an OTP to verify your identity.",
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = secondaryTextColor,
        lineHeight = 17.5.sp
    )

    Spacer(modifier = Modifier.height(18.dp))

    Text(
        text = "Mobile Number *",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = navyColor
    )

    Spacer(modifier = Modifier.height(6.dp))

    OutlinedTextField(
        value = state.mobile,
        onValueChange = { input ->
            val digitsOnly = input.filter { it.isDigit() }.take(10)
            viewModel.setForgotMobile(digitsOnly)
        },
        placeholder = {
            Text(
                text = "Enter 10-digit mobile number",
                color = secondaryTextColor.copy(alpha = 0.65f),
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                tint = if (state.mobileError != null) errorColor else secondaryTextColor,
                modifier = Modifier.size(19.dp)
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onSubmit() }
        ),
        singleLine = true,
        isError = state.mobileError != null,
        supportingText = if (state.mobileError != null) {
            {
                Text(
                    text = state.mobileError!!,
                    color = errorColor,
                    fontSize = 11.5.sp
                )
            }
        } else null,
        textStyle = TextStyle(
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Medium,
            color = navyColor
        ),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = inputBgColor,
            unfocusedContainerColor = inputBgColor,
            focusedBorderColor = goldPrimary,
            unfocusedBorderColor = dividerColor,
            errorBorderColor = errorColor,
            cursorColor = goldPrimary
        ),
        shape = RoundedCornerShape(12.dp)
    )

    Spacer(modifier = Modifier.height(18.dp))

    Button(
        onClick = onSubmit,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        enabled = !state.isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = goldPrimary,
            contentColor = Color.White,
            disabledContainerColor = goldPrimary.copy(alpha = 0.65f),
            disabledContentColor = Color.White.copy(alpha = 0.85f)
        )
    ) {
        if (state.isLoading) {
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
                    text = "Send Verification OTP",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Remember your password? ",
            color = secondaryTextColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = "Login",
            color = goldPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .clickable { onNavigateToLogin() }
                .padding(4.dp)
        )
    }
}

/**
 * Step 2: OTP Verification
 */
@Composable
private fun Step2OtpView(
    state: ForgotPasswordState,
    viewModel: AuthViewModel,
    navyColor: Color,
    secondaryTextColor: Color,
    inputBgColor: Color,
    dividerColor: Color,
    goldPrimary: Color,
    errorColor: Color,
    successColor: Color,
    isDark: Boolean,
    onVerify: () -> Unit,
    onResend: () -> Unit
) {
    Text(
        text = "Verify OTP",
        fontSize = 21.sp,
        fontWeight = FontWeight.ExtraBold,
        color = navyColor
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "Enter the 6-digit verification code sent to +91 ${state.mobile}",
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = secondaryTextColor,
        lineHeight = 17.5.sp
    )

    // Demo/Testing Helper Banner
    val testOtp = state.generatedOtp ?: "123456"
    Spacer(modifier = Modifier.height(10.dp))
    Surface(
        color = if (isDark) Color(0xFF132A1D) else Color(0xFFECFDF5),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF166534) else Color(0xFFA7F3D0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Verification Code (Demo/SMS):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = successColor
                )
                Text(
                    text = testOtp,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = if (isDark) Color.White else Color(0xFF065F46)
                )
            }

            TextButton(
                onClick = { viewModel.setForgotOtp(testOtp) },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Auto-Fill",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = goldPrimary
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text(
        text = "6-Digit OTP *",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = navyColor
    )

    Spacer(modifier = Modifier.height(6.dp))

    OutlinedTextField(
        value = state.otp,
        onValueChange = { input ->
            val digitsOnly = input.filter { it.isDigit() }.take(6)
            viewModel.setForgotOtp(digitsOnly)
        },
        placeholder = {
            Text(
                text = "Enter 6-digit OTP",
                color = secondaryTextColor.copy(alpha = 0.65f),
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = if (state.otpError != null) errorColor else secondaryTextColor,
                modifier = Modifier.size(19.dp)
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onVerify() }
        ),
        singleLine = true,
        isError = state.otpError != null,
        supportingText = if (state.otpError != null) {
            {
                Text(
                    text = state.otpError!!,
                    color = errorColor,
                    fontSize = 11.5.sp
                )
            }
        } else null,
        textStyle = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            color = navyColor
        ),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = inputBgColor,
            unfocusedContainerColor = inputBgColor,
            focusedBorderColor = goldPrimary,
            unfocusedBorderColor = dividerColor,
            errorBorderColor = errorColor,
            cursorColor = goldPrimary
        ),
        shape = RoundedCornerShape(12.dp)
    )

    Spacer(modifier = Modifier.height(18.dp))

    Button(
        onClick = onVerify,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        enabled = !state.isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = goldPrimary,
            contentColor = Color.White,
            disabledContainerColor = goldPrimary.copy(alpha = 0.65f),
            disabledContentColor = Color.White.copy(alpha = 0.85f)
        )
    ) {
        if (state.isLoading) {
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
                    text = "Verify Code",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Didn't receive OTP?",
            color = secondaryTextColor,
            fontSize = 12.5.sp
        )

        Text(
            text = "Resend OTP",
            color = goldPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.5.sp,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .clickable { onResend() }
                .padding(4.dp)
        )
    }
}

/**
 * Step 3: Set New Password
 */
@Composable
private fun Step3NewPasswordView(
    state: ForgotPasswordState,
    viewModel: AuthViewModel,
    navyColor: Color,
    secondaryTextColor: Color,
    inputBgColor: Color,
    dividerColor: Color,
    goldPrimary: Color,
    errorColor: Color,
    successColor: Color,
    isDark: Boolean,
    onSubmit: () -> Unit
) {
    Text(
        text = "Set New Password",
        fontSize = 21.sp,
        fontWeight = FontWeight.ExtraBold,
        color = navyColor
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "Create a strong, new password for your account. You will use this password to log in.",
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = secondaryTextColor,
        lineHeight = 17.5.sp
    )

    Spacer(modifier = Modifier.height(14.dp))

    // New Password Input
    Text(
        text = "New Password *",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = navyColor
    )

    Spacer(modifier = Modifier.height(6.dp))

    OutlinedTextField(
        value = state.newPassword,
        onValueChange = { viewModel.setForgotNewPassword(it) },
        placeholder = {
            Text(
                text = "Enter new password",
                color = secondaryTextColor.copy(alpha = 0.65f),
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = if (state.passwordError != null) errorColor else secondaryTextColor,
                modifier = Modifier.size(19.dp)
            )
        },
        trailingIcon = {
            IconButton(onClick = { viewModel.toggleForgotNewPasswordVisible() }) {
                Icon(
                    imageVector = if (state.isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    contentDescription = if (state.isPasswordVisible) "Hide password" else "Show password",
                    tint = secondaryTextColor,
                    modifier = Modifier.size(19.dp)
                )
            }
        },
        visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        ),
        singleLine = true,
        isError = state.passwordError != null,
        supportingText = if (state.passwordError != null) {
            {
                Text(
                    text = state.passwordError!!,
                    color = errorColor,
                    fontSize = 11.5.sp
                )
            }
        } else null,
        textStyle = TextStyle(
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Medium,
            color = navyColor
        ),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = inputBgColor,
            unfocusedContainerColor = inputBgColor,
            focusedBorderColor = goldPrimary,
            unfocusedBorderColor = dividerColor,
            errorBorderColor = errorColor,
            cursorColor = goldPrimary
        ),
        shape = RoundedCornerShape(12.dp)
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Confirm Password Input
    Text(
        text = "Confirm New Password *",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = navyColor
    )

    Spacer(modifier = Modifier.height(6.dp))

    OutlinedTextField(
        value = state.confirmPassword,
        onValueChange = { viewModel.setForgotConfirmPassword(it) },
        placeholder = {
            Text(
                text = "Re-enter new password",
                color = secondaryTextColor.copy(alpha = 0.65f),
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = if (state.confirmPasswordError != null) errorColor else secondaryTextColor,
                modifier = Modifier.size(19.dp)
            )
        },
        trailingIcon = {
            IconButton(onClick = { viewModel.toggleForgotConfirmPasswordVisible() }) {
                Icon(
                    imageVector = if (state.isConfirmPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    contentDescription = if (state.isConfirmPasswordVisible) "Hide password" else "Show password",
                    tint = secondaryTextColor,
                    modifier = Modifier.size(19.dp)
                )
            }
        },
        visualTransformation = if (state.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onSubmit() }
        ),
        singleLine = true,
        isError = state.confirmPasswordError != null,
        supportingText = if (state.confirmPasswordError != null) {
            {
                Text(
                    text = state.confirmPasswordError!!,
                    color = errorColor,
                    fontSize = 11.5.sp
                )
            }
        } else null,
        textStyle = TextStyle(
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Medium,
            color = navyColor
        ),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = inputBgColor,
            unfocusedContainerColor = inputBgColor,
            focusedBorderColor = goldPrimary,
            unfocusedBorderColor = dividerColor,
            errorBorderColor = errorColor,
            cursorColor = goldPrimary
        ),
        shape = RoundedCornerShape(12.dp)
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Live Password Strength Checklist Box
    LivePasswordRulesCard(
        password = state.newPassword,
        confirmPassword = state.confirmPassword,
        isDark = isDark,
        successColor = successColor,
        secondaryColor = secondaryTextColor
    )

    Spacer(modifier = Modifier.height(18.dp))

    Button(
        onClick = onSubmit,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        enabled = !state.isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = goldPrimary,
            contentColor = Color.White,
            disabledContainerColor = goldPrimary.copy(alpha = 0.65f),
            disabledContentColor = Color.White.copy(alpha = 0.85f)
        )
    ) {
        if (state.isLoading) {
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
                    text = "Reset Password",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Live Password Rules Validation Box
 */
@Composable
private fun LivePasswordRulesCard(
    password: String,
    confirmPassword: String,
    isDark: Boolean,
    successColor: Color,
    secondaryColor: Color
) {
    val hasMinLength = RegistrationFormData.isMinLengthMet(password)
    val hasUpperCase = RegistrationFormData.isUpperCaseMet(password)
    val hasLowerCase = RegistrationFormData.isLowerCaseMet(password)
    val hasDigit = RegistrationFormData.isDigitMet(password)
    val matchesConfirm = password.isNotEmpty() && password == confirmPassword

    val boxBg = if (isDark) Color(0xFF0D1E16) else Color(0xFFF0FDF4)
    val boxBorder = if (isDark) Color(0xFF166534) else Color(0xFFBBF7D0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = boxBg),
        border = BorderStroke(1.dp, boxBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "Password requirements:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = successColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            RuleRow(isMet = hasMinLength, text = "At least 8 characters", successColor = successColor, unfulfilledColor = secondaryColor)
            RuleRow(isMet = hasUpperCase, text = "One uppercase letter (A–Z)", successColor = successColor, unfulfilledColor = secondaryColor)
            RuleRow(isMet = hasLowerCase, text = "One lowercase letter (a–z)", successColor = successColor, unfulfilledColor = secondaryColor)
            RuleRow(isMet = hasDigit, text = "One number (0–9)", successColor = successColor, unfulfilledColor = secondaryColor)
            if (confirmPassword.isNotEmpty()) {
                RuleRow(isMet = matchesConfirm, text = "Passwords match", successColor = successColor, unfulfilledColor = secondaryColor)
            }
        }
    }
}

@Composable
private fun RuleRow(
    isMet: Boolean,
    text: String,
    successColor: Color,
    unfulfilledColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.Check,
            contentDescription = null,
            tint = if (isMet) successColor else unfulfilledColor.copy(alpha = 0.5f),
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 11.5.sp,
            color = if (isMet) successColor else unfulfilledColor,
            fontWeight = if (isMet) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

/**
 * Step 4: Success Confirmation View
 */
@Composable
private fun Step4SuccessView(
    message: String,
    navyColor: Color,
    secondaryTextColor: Color,
    goldPrimary: Color,
    successColor: Color,
    isDark: Boolean,
    onNavigateToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = if (isDark) Color(0xFF133221) else Color(0xFFDCFCE7),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = successColor,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Password Reset Successful!",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = navyColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium,
            color = secondaryTextColor,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNavigateToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = goldPrimary,
                contentColor = Color.White
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Back to Login",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
