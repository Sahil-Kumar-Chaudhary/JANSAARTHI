package com.example.jansaarthi.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.core.view.WindowCompat
import com.example.jansaarthi.R

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightPrimary,
    
    secondary = LightAccent,
    onSecondary = Color.White,
    secondaryContainer = LightAccent.copy(alpha = 0.15f),
    onSecondaryContainer = LightAccent,
    
    tertiary = LightSuccess, // Using tertiary for Success state in components
    onTertiary = Color.White,
    tertiaryContainer = LightSuccess.copy(alpha = 0.15f),
    onTertiaryContainer = LightSuccess,
    
    error = LightError,
    onError = Color.White,
    errorContainer = LightError.copy(alpha = 0.15f),
    onErrorContainer = LightError,
    
    background = LightBackground,
    onBackground = LightTextPrimary,
    
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurface,
    onSurfaceVariant = LightTextSecondary,
    
    outline = LightOutline,
    outlineVariant = LightOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkBackground,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkPrimary,
    
    secondary = DarkAccent,
    onSecondary = DarkBackground,
    secondaryContainer = DarkAccent.copy(alpha = 0.15f),
    onSecondaryContainer = DarkAccent,
    
    tertiary = DarkSuccess,
    onTertiary = DarkBackground,
    tertiaryContainer = DarkSuccess.copy(alpha = 0.15f),
    onTertiaryContainer = DarkSuccess,
    
    error = DarkError,
    onError = DarkBackground,
    errorContainer = DarkError.copy(alpha = 0.15f),
    onErrorContainer = DarkError,
    
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkTextSecondary,
    
    outline = DarkOutline,
    outlineVariant = DarkOutline
)

val LocalIsDarkTheme = staticCompositionLocalOf { false }

@Composable
fun isAppInDarkTheme(): Boolean = LocalIsDarkTheme.current

@Composable
fun getAppLogoRes(): Int = if (LocalIsDarkTheme.current) {
    R.drawable.jansaarthi_logo_dark
} else {
    R.drawable.jansaarthi_logo_light
}

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    contentDescription: String? = "JANSAARTHI Logo"
) {
    androidx.compose.foundation.Image(
        painter = painterResource(id = getAppLogoRes()),
        contentDescription = contentDescription,
        modifier = modifier
    )
}

@Composable
fun JANSAARTHITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
