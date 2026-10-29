package com.example.jansaarthi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.jansaarthi.data.repository.ThemeMode
import com.example.jansaarthi.data.repository.AppLanguage
import com.example.jansaarthi.data.network.RetrofitClient
import com.example.jansaarthi.ui.navigation.AppNavGraph
import com.example.jansaarthi.ui.navigation.Routes
import com.example.jansaarthi.ui.theme.JANSAARTHITheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { false }
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        
        // Controlled debug environment selection via Intent extra (e.g. adb am start --es environment production)
        if (BuildConfig.DEBUG) {
            val envExtra = intent?.getStringExtra("backend_environment") ?: intent?.getStringExtra("environment")
            if (!envExtra.isNullOrBlank()) {
                val target = when (envExtra.lowercase()) {
                    "production", "prod" -> RetrofitClient.EnvironmentTarget.PRODUCTION
                    "staging", "stage" -> RetrofitClient.EnvironmentTarget.STAGING
                    "emulator", "local" -> RetrofitClient.EnvironmentTarget.EMULATOR
                    else -> null
                }
                if (target != null) {
                    RetrofitClient.setExplicitDebugEnvironment(this, target)
                }
            }
        }

        val settingsRepo = App.instance.settingsRepository
        
        setContent {
            val themeMode by settingsRepo.themeMode.collectAsState()
            val appLanguage by settingsRepo.appLanguage.collectAsState()
            
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            
            LaunchedEffect(appLanguage) {
                val langTag = if (appLanguage == AppLanguage.HI) "hi" else "en"
                val appLocale = LocaleListCompat.forLanguageTags(langTag)
                AppCompatDelegate.setApplicationLocales(appLocale)
            }
            
            JANSAARTHITheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                val startDest = intent?.getStringExtra("start_destination") ?: Routes.SPLASH
                AppNavGraph(navController = navController, startDestination = startDest)
            }
        }
    }
}
