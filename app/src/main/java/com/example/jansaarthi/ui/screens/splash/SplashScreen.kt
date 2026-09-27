package com.example.jansaarthi.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jansaarthi.R
import com.example.jansaarthi.ui.theme.AppLogo
import com.example.jansaarthi.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToOnboarding: () -> Unit = {}
) {
    val isDark = isAppInDarkTheme()

    // Brand Design Tokens
    val bgColor = if (isDark) Color(0xFF0B1220) else Color(0xFFFAF8F2)
    val navyColor = if (isDark) Color(0xFFF5F7FA) else Color(0xFF17243A)
    val goldPrimary = Color(0xFFC89124)
    val goldAccent = Color(0xFFD6A13A)
    val supportingTextColor = if (isDark) Color(0xFFAAB4C3) else Color(0xFF657080)
    val dividerTrackColor = if (isDark) Color(0xFF344155) else Color(0xFFE7E0D2)

    var startAnimation by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.90f,
        animationSpec = tween(500, easing = EaseOutBack),
        label = "LogoScale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(400, easing = EaseOutCubic),
        label = "ContentAlpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2300)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        // Decorative Background Layer (Top-Left Golden Waves, Top-Right Tricolour Ribbon, Bottom Golden Waves)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawTopLeftGoldenWaves(goldPrimary, goldAccent, isDark)
            drawTricolourWave(isDark)
            drawBottomGoldenWaves(goldPrimary, goldAccent, isDark)
        }

        // Low-Opacity Landmark Silhouettes (India Gate, Parliament, Rashtrapati Bhavan)
        Image(
            painter = painterResource(id = R.drawable.ic_indian_landmarks_silhouette),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp),
            colorFilter = ColorFilter.tint(
                if (isDark) goldAccent.copy(alpha = 0.20f) else goldPrimary.copy(alpha = 0.32f)
            ),
            contentScale = ContentScale.FillWidth
        )

        // Main Brand Identity Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .offset(y = (-20).dp) // Balanced optical center
                .alpha(contentAlpha)
        ) {
            // JANSAARTHI Logo
            Box(
                modifier = Modifier
                    .size(142.dp)
                    .scale(logoScale),
                contentAlignment = Alignment.Center
            ) {
                AppLogo(modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Brand Name
            Text(
                text = "JANSAARTHI",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = navyColor,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Tagline
            Text(
                text = "Aapki Zarurat. Sahi Sarkari Raasta.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = goldPrimary,
                letterSpacing = 0.2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Decorative Golden Divider with Subtle Central Ornament
            DecorativeGoldenDivider(
                goldColor = goldPrimary,
                accentColor = goldAccent
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Supporting Text
            Text(
                text = "Smart guidance for government\nfinancial assistance",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = supportingTextColor,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Animated Golden Circular Loading Indicator
            SplashLoadingIndicator(
                goldColor = goldPrimary,
                accentColor = goldAccent,
                trackColor = dividerTrackColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Loading... Text
            Text(
                text = "Loading...",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Normal,
                color = supportingTextColor,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Draws soft golden decorative layered curves in the upper-left corner.
 */
private fun DrawScope.drawTopLeftGoldenWaves(
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

    // Elegant contour outline
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
        moveTo(0f, h - 120.dp.toPx())
        cubicTo(
            w * 0.35f, h - 155.dp.toPx(),
            w * 0.65f, h - 105.dp.toPx(),
            w, h - 138.dp.toPx()
        )
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = wave1,
        brush = Brush.verticalGradient(
            colors = listOf(
                goldAccent.copy(alpha = if (isDark) 0.20f else 0.32f),
                goldPrimary.copy(alpha = if (isDark) 0.38f else 0.58f)
            ),
            startY = h - 155.dp.toPx(),
            endY = h
        )
    )

    // Layer 2: Mid golden wave
    val wave2 = Path().apply {
        moveTo(0f, h - 85.dp.toPx())
        cubicTo(
            w * 0.28f, h - 70.dp.toPx(),
            w * 0.72f, h - 110.dp.toPx(),
            w, h - 75.dp.toPx()
        )
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = wave2,
        brush = Brush.verticalGradient(
            colors = listOf(
                goldAccent.copy(alpha = if (isDark) 0.45f else 0.72f),
                goldPrimary.copy(alpha = if (isDark) 0.65f else 0.92f)
            ),
            startY = h - 110.dp.toPx(),
            endY = h
        )
    )

    // Layer 3: Foreground rich golden wave
    val wave3 = Path().apply {
        moveTo(0f, h - 50.dp.toPx())
        cubicTo(
            w * 0.38f, h - 80.dp.toPx(),
            w * 0.75f, h - 40.dp.toPx(),
            w, h - 18.dp.toPx()
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
            startY = h - 80.dp.toPx(),
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
                .width(68.dp)
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
                .width(68.dp)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(accentColor.copy(alpha = 0.75f), Color.Transparent)
                    )
                )
        )
    }
}

/**
 * A small animated golden circular loading indicator.
 */
@Composable
private fun SplashLoadingIndicator(
    modifier: Modifier = Modifier,
    goldColor: Color,
    accentColor: Color,
    trackColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SplashSpinnerTransition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SplashSpinnerRotation"
    )

    Canvas(modifier = modifier.size(32.dp)) {
        val strokeWidth = 3.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
        val arcSize = Size(diameter, diameter)

        // Subtle background circle track
        drawCircle(
            color = trackColor.copy(alpha = 0.40f),
            radius = diameter / 2f,
            center = center,
            style = Stroke(width = strokeWidth)
        )

        // Smooth rotating sweep arc
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color.Transparent,
                    accentColor.copy(alpha = 0.40f),
                    goldColor
                )
            ),
            startAngle = rotation,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
