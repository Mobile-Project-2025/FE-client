package com.example.myapplication.data

import retrofit2.http.GET
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


// 미션 조회 API
interface Mission_API {

    // 상시미션 조회
    @GET("api/missions/regular")
    suspend fun getRegularMissions(): List<Mission_Data>

    // 돌발미션 조회
    @GET("api/missions/event")
    suspend fun getEventMissions(): List<Mission_Data>

    @GET("api/missions/pending")
    suspend fun getPendingMissions(): List<PendingMission>

// 미션 인증 사진 업로드 API (미완)
//    @Multipart
//    @POST("api/missions/participate")
//    suspend fun uploadMissionParticipation(
//        @Part("missionId") missionId: Int,
//        @Part image: MultipartBody.Part
//    ): UploadResponse
// 참여 이력 리스트 조회
    @GET("api/missions/history")
    suspend fun getMissionHistory(): List<MissionHistoryItem>

    // 참여 이력 상세 조회
    @GET("api/missions/history/{participationId}")
    suspend fun getMissionHistoryDetail(
        @retrofit2.http.Path("participationId") participationId: Long
    ): MissionHistoryDetail
}