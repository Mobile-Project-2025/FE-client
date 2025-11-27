package com.example.myapplication.ui.Log_in

import android.content.Context
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object HttpClientProvider {

    @Volatile
    private var client: OkHttpClient? = null

    fun get(context: Context): OkHttpClient {
        val cached = client
        if (cached != null) return cached

        val newClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context.applicationContext))
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        client = newClient
        return newClient
    }
}
