package com.example.jansaarthi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.jansaarthi.data.repository.ThemeMode
import com.example.jansaarthi.data.repository.AppLanguage
import com.example.jansaarthi.ui.navigation.AppNavGraph
import com.example.jansaarthi.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { false }
        super.onCreate(savedInstanceState)
        
        val settingsRepo = App.instance.settingsRepository
        
        setContent {
            val themeMode by settingsRepo.themeMode.collectAsState()
            val appLanguage by settingsRepo.appLanguage.collectAsState()
            
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            
            val strings = if (appLanguage == AppLanguage.HI) HindiStrings else EnglishStrings
            
            CompositionLocalProvider(LocalAppStrings provides strings) {
                JANSAARTHITheme(darkTheme = darkTheme) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
                }
            }
        }
    }
}
