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
        
        // Switch debug app to production backend using existing RetrofitClient environment-switching method
        com.example.jansaarthi.data.network.RetrofitClient.setEnvironment(
            com.example.jansaarthi.data.network.RetrofitClient.EnvironmentTarget.PRODUCTION
        )
        android.util.Log.i("App", "RetrofitClient target switched to PRODUCTION: ${com.example.jansaarthi.data.network.RetrofitClient.getBaseUrl()}")

        val tokenManager = com.example.jansaarthi.data.local.TokenManager(this)
        authRepository = com.example.jansaarthi.data.repository.AuthRepository(tokenManager)
        ragRepository = com.example.jansaarthi.data.repository.RagRepository()
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
