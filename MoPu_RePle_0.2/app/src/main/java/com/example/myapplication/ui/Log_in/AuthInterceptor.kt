package com.example.myapplication.ui.Log_in

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {

    private val excludedPaths = listOf(
        "/api/auth/login",
        "/api/auth/signup"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val urlPath = original.url.encodedPath

        val shouldAttach = excludedPaths.none { urlPath.endsWith(it) }

        val reqBuilder = original.newBuilder()
            .header("Accept", "application/json")

        if (shouldAttach) {
            val token = TokenStore.getToken(context)
            if (!token.isNullOrBlank()) {
                reqBuilder.header("Authorization", "Bearer $token")
            }
        }

        val request = reqBuilder.build()
        val response = chain.proceed(request)

        Log.d(
            "HTTP",
            "url=${response.request.url}, method=${response.request.method}, code=${response.code}"
        )

        if (response.code == 401) {
            TokenStore.clear(context)
        }
        return response
    }
}
