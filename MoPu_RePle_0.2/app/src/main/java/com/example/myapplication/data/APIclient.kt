// ApiClient.kt 예시
package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.ui.Log_in.HttpClientProvider
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


// API 호출 주소
object ApiClient {
    private const val BASE_URL = "http://43.202.225.195:8080/"

    @Volatile
    private var retrofit: Retrofit? = null

    fun getMissionsApi(context: Context): Mission_API {
        val client = HttpClientProvider.get(context)

        val r = retrofit ?: Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit = r
        return r.create(Mission_API::class.java)
    }

    private fun getClient(context: Context): Retrofit {
        return retrofit ?: synchronized(this) {
            val client = HttpClientProvider.get(context)
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .also { retrofit = it }
        }
    }

    fun getUserApi(context: Context): User_API {
        return getClient(context).create(User_API::class.java)
    }
}