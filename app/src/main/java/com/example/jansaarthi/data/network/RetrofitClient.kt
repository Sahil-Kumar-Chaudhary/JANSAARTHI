package com.example.jansaarthi.data.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    const val PROD_BASE_URL = "https://jansaarthi-backend.onrender.com/"
    const val EMULATOR_BASE_URL = "http://10.0.2.2:5000/"

    private var currentBaseUrl = PROD_BASE_URL

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

    fun getBaseUrl(): String = currentBaseUrl

    val instance: AuthApi
        get() = retrofit.create(AuthApi::class.java)

    val ragApi: RagApi
        get() = retrofit.create(RagApi::class.java)
}
