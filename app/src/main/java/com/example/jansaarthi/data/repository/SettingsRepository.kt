package com.example.jansaarthi.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode { LIGHT, DARK, SYSTEM }
enum class AppLanguage { EN, HI }

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("jansaarthi_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _appLanguage = MutableStateFlow(
        AppLanguage.valueOf(prefs.getString("app_language", AppLanguage.EN.name) ?: AppLanguage.EN.name)
    )
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _themeMode.value = mode
    }

    fun setAppLanguage(lang: AppLanguage) {
        prefs.edit().putString("app_language", lang.name).apply()
        _appLanguage.value = lang
    }
}
