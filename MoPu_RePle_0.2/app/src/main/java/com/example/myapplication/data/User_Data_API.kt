package com.example.myapplication.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface User_API {
    // 내 정보 가져오기
    @GET("api/users/me")
    suspend fun getUserProfile(): User_Data

    // [추가됨] 포인트 수정하기 (PATCH)
    // userId: 누구의 포인트인지
    // body: 변경할 포인트 값 (PointRequest)
    @PATCH("api/users/{userId}/points")
    suspend fun updateUserPoint(
        @Path("userId") userId: Long,
        @Body body: PointRequest
    ): User_Data  // 응답으로 갱신된 유저 정보를 다시 준다고 가정
}