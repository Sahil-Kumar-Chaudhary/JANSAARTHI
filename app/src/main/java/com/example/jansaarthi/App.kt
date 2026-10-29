package com.example.jansaarthi

import android.app.Application
import com.example.jansaarthi.data.repository.SettingsRepository

class App : Application() {
    lateinit var settingsRepository: SettingsRepository
        private set
        
    lateinit var authRepository: com.example.jansaarthi.data.repository.AuthRepository
        private set

    lateinit var ragRepository: com.example.jansaarthi.data.repository.RagRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        settingsRepository = SettingsRepository(this)
        
        // Initialize backend environment safely:
        // - Release builds ALWAYS route to Production HTTPS
        // - Debug builds default safely to Staging on physical devices (and Emulator on emulators)
        // - Controlled environment override is permitted only in debug builds via explicit selection
        com.example.jansaarthi.data.network.RetrofitClient.initEnvironment(this)

        val tokenManager = com.example.jansaarthi.data.local.TokenManager(this)
        authRepository = com.example.jansaarthi.data.repository.AuthRepository(tokenManager)
        ragRepository = com.example.jansaarthi.data.repository.RagRepository()
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
