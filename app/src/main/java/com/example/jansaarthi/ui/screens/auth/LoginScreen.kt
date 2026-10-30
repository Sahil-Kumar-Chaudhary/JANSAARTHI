package com.example.jansaarthi.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
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

    var mobileNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var mobileError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var showForgotDialog by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            viewModel.resetState()
            onNavigateToHome()
        }
    }

    fun submitLogin() {
        keyboardController?.hide()
        focusManager.clearFocus()

        var hasError = false
        val trimmedMobile = mobileNumber.trim()
        val trimmedPassword = password.trim()

        if (trimmedMobile.isEmpty()) {
            mobileError = "Mobile number is required"
            hasError = true
        } else if (!trimmedMobile.matches(Regex("^[0-9]{10}$"))) {
            mobileError = "Enter a valid 10-digit mobile number"
            hasError = true
        }

        if (trimmedPassword.isEmpty()) {
            passwordError = "Password is required"
            hasError = true
        }

        if (!hasError && !isLoading) {
            viewModel.login(trimmedMobile, trimmedPassword)
        }
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = {
                Text(
                    text = "Forgot Password",
                    fontWeight = FontWeight.Bold,
                    color = navyColor,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Please contact support or follow the recovery process to reset your password.",
                    color = secondaryTextColor,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text(
                        text = "OK",
                        color = goldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = cardBgColor,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Decorative Background Layer (Top-Left Golden Curves, Top-Right Tricolour Wave, Bottom Golden Waves)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawTopLeftGoldenCurves(goldPrimary, goldAccent, isDark)
            drawTricolourWave(isDark)
            drawBottomGoldenWaves(goldPrimary, goldAccent, isDark)
        }

        // Low-Opacity Landmark Silhouettes (India Gate, Parliament, Rashtrapati Bhavan)
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

        // Foreground Scrollable Login Form
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Branding Section: JANSAARTHI Logo
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                AppLogo(modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Brand Name
            Text(
                text = "JANSAARTHI",
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                color = navyColor,
                letterSpacing = 1.8.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Tagline
            Text(
                text = "Aapki Zarurat. Sahi Sarkari Raasta.",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = goldPrimary,
                letterSpacing = 0.2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Thin Decorative Golden Divider with Central Diamond Ornament
            DecorativeGoldenDivider(
                goldColor = goldPrimary,
                accentColor = goldAccent
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Supporting Text
            Text(
                text = "Smart guidance for government\nfinancial assistance",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = secondaryTextColor,
                lineHeight = 16.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Login Section Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardBgColor
                ),
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
                    // Heading: Welcome Back
                    Text(
                        text = "Welcome Back",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = navyColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Subtitle: Login to continue to JANSAARTHI
                    Text(
                        text = "Login to continue to JANSAARTHI",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = secondaryTextColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mobile Number Input Field
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = {
                            mobileNumber = it
                            mobileError = null
                            if (authState is AuthState.Error) viewModel.resetState()
                        },
                        placeholder = {
                            Text(
                                text = "Enter mobile number",
                                color = secondaryTextColor.copy(alpha = 0.65f),
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = if (mobileError != null) errorColor else secondaryTextColor,
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true,
                        isError = mobileError != null,
                        supportingText = if (mobileError != null) {
                            {
                                Text(
                                    text = mobileError!!,
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // Password Input Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                            if (authState is AuthState.Error) viewModel.resetState()
                        },
                        placeholder = {
                            Text(
                                text = "Enter password",
                                color = secondaryTextColor.copy(alpha = 0.65f),
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (passwordError != null) errorColor else secondaryTextColor,
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = secondaryTextColor,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { submitLogin() }
                        ),
                        singleLine = true,
                        isError = passwordError != null,
                        supportingText = if (passwordError != null) {
                            {
                                Text(
                                    text = passwordError!!,
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

                    // Backend Authentication Error Banner
                    if (authState is AuthState.Error) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = errorColor.copy(alpha = if (isDark) 0.18f else 0.10f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, errorColor.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = (authState as AuthState.Error).message,
                                color = errorColor,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Full-Width Golden Login Button with Arrow
                    Button(
                        onClick = { submitLogin() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = goldPrimary,
                            contentColor = Color.White,
                            disabledContainerColor = goldPrimary.copy(alpha = 0.65f),
                            disabledContentColor = Color.White.copy(alpha = 0.85f)
                        )
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
                                    text = "Login",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Forgot Password Action (Centered)
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Forgot Password?",
                            color = navyColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp,
                            modifier = Modifier
                                .clickable { showForgotDialog = true }
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer Link: Don't have an account? Register
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don’t have an account? ",
                    color = secondaryTextColor,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = "Register",
                    color = goldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { onNavigateToRegister() }
                        .padding(horizontal = 2.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Draws soft golden decorative layered curves in the upper-left corner.
 */
private fun DrawScope.drawTopLeftGoldenCurves(
    goldPrimary: Color,
    goldAccent: Color,
    isDark: Boolean
) {
    val w = size.width
    val h = size.height

    // Outer broad soft curve
    val outerPath = Path().apply {
        moveTo(0f, 0f)
        lineTo(w * 0.48f, 0f)
        cubicTo(
            w * 0.36f, h * 0.08f,
            w * 0.12f, h * 0.18f,
            0f, h * 0.24f
        )
        close()
    }
    val outerAlpha = if (isDark) 0.10f else 0.16f
    drawPath(
        path = outerPath,
        brush = Brush.radialGradient(
            colors = listOf(
                goldAccent.copy(alpha = outerAlpha),
                goldAccent.copy(alpha = outerAlpha * 0.3f),
                Color.Transparent
            ),
            center = Offset(0f, 0f),
            radius = w * 0.55f
        )
    )

    // Middle layered wave
    val midPath = Path().apply {
        moveTo(0f, 0f)
        lineTo(w * 0.32f, 0f)
        cubicTo(
            w * 0.22f, h * 0.06f,
            w * 0.08f, h * 0.13f,
            0f, h * 0.17f
        )
        close()
    }
    val midAlpha = if (isDark) 0.14f else 0.22f
    drawPath(
        path = midPath,
        brush = Brush.radialGradient(
            colors = listOf(
                goldPrimary.copy(alpha = midAlpha),
                goldAccent.copy(alpha = midAlpha * 0.4f),
                Color.Transparent
            ),
            center = Offset(0f, 0f),
            radius = w * 0.38f
        )
    )

    // Contour outline
    val contourPath = Path().apply {
        moveTo(w * 0.40f, 0f)
        cubicTo(
            w * 0.29f, h * 0.07f,
            w * 0.10f, h * 0.16f,
            0f, h * 0.20f
        )
    }
    drawPath(
        path = contourPath,
        brush = Brush.linearGradient(
            colors = listOf(
                goldAccent.copy(alpha = if (isDark) 0.25f else 0.38f),
                goldPrimary.copy(alpha = if (isDark) 0.12f else 0.20f),
                Color.Transparent
            ),
            start = Offset(w * 0.40f, 0f),
            end = Offset(0f, h * 0.20f)
        ),
        style = Stroke(width = 1.5.dp.toPx())
    )
}

/**
 * Draws an Indian tricolour-inspired flowing wave in the upper-right corner.
 */
private fun DrawScope.drawTricolourWave(isDark: Boolean) {
    val w = size.width
    val h = size.height
    val stripeWidth = 8.5.dp.toPx()
    val baseAlpha = if (isDark) 0.88f else 0.95f

    val saffron = Color(0xFFFF9933)
    val white = Color(0xFFFFFFFF)
    val green = Color(0xFF138808)

    // Saffron stripe (top)
    val saffronPath = Path().apply {
        moveTo(w * 0.54f, 0f)
        cubicTo(
            w * 0.70f, h * 0.040f,
            w * 0.86f, h * 0.082f,
            w + 8.dp.toPx(), h * 0.076f
        )
    }
    drawPath(
        path = saffronPath,
        color = saffron.copy(alpha = baseAlpha),
        style = Stroke(width = stripeWidth, cap = StrokeCap.Round)
    )

    // White stripe (middle)
    val whitePath = Path().apply {
        moveTo(w * 0.56f, stripeWidth * 0.92f)
        cubicTo(
            w * 0.71f, h * 0.040f + stripeWidth * 0.92f,
            w * 0.87f, h * 0.082f + stripeWidth * 0.92f,
            w + 8.dp.toPx(), h * 0.076f + stripeWidth * 0.92f
        )
    }
    drawPath(
        path = whitePath,
        color = white.copy(alpha = baseAlpha),
        style = Stroke(width = stripeWidth, cap = StrokeCap.Round)
    )

    // Green stripe (bottom)
    val greenPath = Path().apply {
        moveTo(w * 0.58f, stripeWidth * 1.84f)
        cubicTo(
            w * 0.72f, h * 0.040f + stripeWidth * 1.84f,
            w * 0.88f, h * 0.082f + stripeWidth * 1.84f,
            w + 8.dp.toPx(), h * 0.076f + stripeWidth * 1.84f
        )
    }
    drawPath(
        path = greenPath,
        color = green.copy(alpha = baseAlpha),
        style = Stroke(width = stripeWidth, cap = StrokeCap.Round)
    )
}

/**
 * Draws flowing golden layered waves along the bottom edge.
 */
private fun DrawScope.drawBottomGoldenWaves(
    goldPrimary: Color,
    goldAccent: Color,
    isDark: Boolean
) {
    val w = size.width
    val h = size.height

    // Layer 1: Back soft golden wave
    val wave1 = Path().apply {
        moveTo(0f, h - 75.dp.toPx())
        cubicTo(
            w * 0.35f, h - 95.dp.toPx(),
            w * 0.65f, h - 65.dp.toPx(),
            w, h - 85.dp.toPx()
        )
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = wave1,
        brush = Brush.verticalGradient(
            colors = listOf(
                goldAccent.copy(alpha = if (isDark) 0.20f else 0.35f),
                goldPrimary.copy(alpha = if (isDark) 0.38f else 0.60f)
            ),
            startY = h - 95.dp.toPx(),
            endY = h
        )
    )

    // Layer 2: Mid golden wave
    val wave2 = Path().apply {
        moveTo(0f, h - 55.dp.toPx())
        cubicTo(
            w * 0.28f, h - 45.dp.toPx(),
            w * 0.72f, h - 70.dp.toPx(),
            w, h - 48.dp.toPx()
        )
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = wave2,
        brush = Brush.verticalGradient(
            colors = listOf(
                goldAccent.copy(alpha = if (isDark) 0.45f else 0.75f),
                goldPrimary.copy(alpha = if (isDark) 0.65f else 0.95f)
            ),
            startY = h - 70.dp.toPx(),
            endY = h
        )
    )

    // Layer 3: Foreground rich golden wave
    val wave3 = Path().apply {
        moveTo(0f, h - 35.dp.toPx())
        cubicTo(
            w * 0.38f, h - 52.dp.toPx(),
            w * 0.75f, h - 28.dp.toPx(),
            w, h - 12.dp.toPx()
        )
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = wave3,
        brush = Brush.verticalGradient(
            colors = listOf(
                goldAccent,
                goldPrimary,
                Color(0xFFB87E18)
            ),
            startY = h - 52.dp.toPx(),
            endY = h
        )
    )
}

/**
 * A thin decorative golden divider with subtle central ornament.
 */
@Composable
private fun DecorativeGoldenDivider(
    modifier: Modifier = Modifier,
    goldColor: Color,
    accentColor: Color
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Left tapering golden line
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, accentColor.copy(alpha = 0.75f))
                    )
                )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Center delicate ornament (diamond with star & flanking accent points)
        Canvas(modifier = Modifier.size(24.dp, 16.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            // Central 4-point diamond
            val diamondPath = Path().apply {
                moveTo(cx, cy - 6.5.dp.toPx())
                lineTo(cx + 4.5.dp.toPx(), cy)
                lineTo(cx, cy + 6.5.dp.toPx())
                lineTo(cx - 4.5.dp.toPx(), cy)
                close()
            }
            drawPath(diamondPath, color = goldColor)

            // Inner light highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = 1.2.dp.toPx(),
                center = Offset(cx, cy)
            )

            // Delicate flanking dots
            drawCircle(
                color = accentColor,
                radius = 1.dp.toPx(),
                center = Offset(cx - 8.5.dp.toPx(), cy)
            )
            drawCircle(
                color = accentColor,
                radius = 1.dp.toPx(),
                center = Offset(cx + 8.5.dp.toPx(), cy)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right tapering golden line
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(accentColor.copy(alpha = 0.75f), Color.Transparent)
                    )
                )
        )
    }
}
