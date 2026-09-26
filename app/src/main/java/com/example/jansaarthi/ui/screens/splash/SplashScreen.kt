package com.example.jansaarthi.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jansaarthi.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToHome: () -> Unit, onNavigateToOnboarding: () -> Unit = {}) {
    var isLogoVisible by remember { mutableStateOf(false) }
    var textVisible by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (isLogoVisible) 1f else 0.5f,
        animationSpec = tween(1000, easing = EaseOutExpo),
        label = "LogoScale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (isLogoVisible) 1f else 0f,
        animationSpec = tween(800),
        label = "LogoAlpha"
    )

    LaunchedEffect(Unit) {
        delay(300)
        isLogoVisible = true
        delay(600)
        textVisible = true
        delay(2000)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.jansaarthi_logo),
                    contentDescription = "JANSAARTHI Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = textVisible,
                enter = fadeIn(tween(800)) + slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = tween(800, easing = EaseOutCubic)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "JANSAARTHI",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aapki Zarurat. Sahi Sarkari Raasta.",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Smart guidance for government financial assistance",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        // Loading indicator at bottom
        AnimatedVisibility(
            visible = textVisible,
            enter = fadeIn(tween(1000, delayMillis = 500)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }
    }
}
