package com.example.jansaarthi

import android.app.Application
import com.example.jansaarthi.data.repository.SettingsRepository

class App : Application() {
    lateinit var settingsRepository: SettingsRepository
        private set
        
    lateinit var authRepository: com.example.jansaarthi.data.repository.AuthRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        settingsRepository = SettingsRepository(this)
        
        val tokenManager = com.example.jansaarthi.data.local.TokenManager(this)
        authRepository = com.example.jansaarthi.data.repository.AuthRepository(tokenManager)
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
