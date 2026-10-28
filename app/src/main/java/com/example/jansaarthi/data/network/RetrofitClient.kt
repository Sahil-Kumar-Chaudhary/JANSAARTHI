package com.example.jansaarthi.data.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    const val PROD_BASE_URL = "https://jansaarthi-backend.onrender.com/"
    const val STAGING_BASE_URL = "https://jansaarthi-rag-staging.onrender.com/"
    const val EMULATOR_BASE_URL = "http://10.0.2.2:3000/"

    enum class EnvironmentTarget {
        PRODUCTION,
        STAGING,
        EMULATOR
    }

    private fun isRunningOnEmulator(): Boolean {
        return try {
            val hardware = android.os.Build.HARDWARE.lowercase()
            val fingerprint = android.os.Build.FINGERPRINT.lowercase()
            val model = android.os.Build.MODEL.lowercase()
            val product = android.os.Build.PRODUCT.lowercase()
            val device = android.os.Build.DEVICE.lowercase()

            hardware.contains("ranchu")
                    || hardware.contains("goldfish")
                    || model.contains("sdk")
                    || model.contains("emulator")
                    || fingerprint.contains("generic")
                    || fingerprint.contains("emu64")
                    || product.contains("sdk")
                    || device.contains("emu")
        } catch (_: Throwable) {
            false
        }
    }

    private fun determineDefaultBaseUrl(): String {
        return try {
            val isEmu = isRunningOnEmulator()
            val url = if (com.example.jansaarthi.BuildConfig.DEBUG) {
                // In debug builds:
                // - Physical test devices (e.g. OPPO CPH2333) default to STAGING_BASE_URL over HTTPS
                //   to safely test live features and isolate test accounts in jansaarthi_staging.
                // - Emulators default to EMULATOR_BASE_URL for local development.
                if (isEmu) {
                    EMULATOR_BASE_URL
                } else {
                    STAGING_BASE_URL
                }
            } else {
                // Release builds ALWAYS use production
                PROD_BASE_URL
            }
            try {
                android.util.Log.d("RetrofitClient", "Initialized base URL: $url (isEmulator=$isEmu, debug=${com.example.jansaarthi.BuildConfig.DEBUG})")
            } catch (_: Throwable) {}
            url
        } catch (_: Throwable) {
            PROD_BASE_URL
        }
    }

    private var currentBaseUrl = determineDefaultBaseUrl()

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private var retrofit: Retrofit = buildRetrofit(currentBaseUrl)

    private fun buildRetrofit(url: String): Retrofit {
        val safeUrl = if (url.endsWith("/")) url else "$url/"
        return Retrofit.Builder()
            .baseUrl(safeUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun setCustomBaseUrl(url: String) {
        currentBaseUrl = url
        retrofit = buildRetrofit(url)
    }

    fun setEnvironment(target: EnvironmentTarget) {
        val url = when (target) {
            EnvironmentTarget.PRODUCTION -> PROD_BASE_URL
            EnvironmentTarget.STAGING -> STAGING_BASE_URL
            EnvironmentTarget.EMULATOR -> EMULATOR_BASE_URL
        }
        setCustomBaseUrl(url)
    }

    fun getActiveEnvironment(): EnvironmentTarget {
        return when (currentBaseUrl) {
            PROD_BASE_URL -> EnvironmentTarget.PRODUCTION
            STAGING_BASE_URL -> EnvironmentTarget.STAGING
            EMULATOR_BASE_URL -> EnvironmentTarget.EMULATOR
            else -> EnvironmentTarget.STAGING
        }
    }

    fun isStagingActive(): Boolean = currentBaseUrl.startsWith(STAGING_BASE_URL)
    fun isProductionActive(): Boolean = currentBaseUrl.startsWith(PROD_BASE_URL)
    fun isEmulatorActive(): Boolean = currentBaseUrl.startsWith(EMULATOR_BASE_URL)

    fun useStagingBaseUrl() {
        setCustomBaseUrl(STAGING_BASE_URL)
    }

    fun useProdBaseUrl() {
        setCustomBaseUrl(PROD_BASE_URL)
    }

    fun useEmulatorBaseUrl() {
        setCustomBaseUrl(EMULATOR_BASE_URL)
    }

    fun getBaseUrl(): String = currentBaseUrl

    val instance: AuthApi
        get() = retrofit.create(AuthApi::class.java)

    val ragApi: RagApi
        get() = retrofit.create(RagApi::class.java)
}
