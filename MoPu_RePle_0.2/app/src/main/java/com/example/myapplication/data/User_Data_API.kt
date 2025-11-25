package com.example.myapplication.data

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface User_API {
    // 내 정보 가져오기
    @GET("api/users/me")
    suspend fun getUserProfile(): User_Data

    @PATCH("api/users/{userId}/points")
    suspend fun updateUserPoint(
        @Path("userId") userId: Long,
        @Body body: PointRequest
    ): ResponseBody
}