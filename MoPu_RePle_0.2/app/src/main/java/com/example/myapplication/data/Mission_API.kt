package com.example.myapplication.data

import retrofit2.http.GET
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path


// 미션 조회 API
interface Mission_API {


    // STUDENT
    // 승인 대기 미션 조회
    @GET("api/missions/pending")
    suspend fun getPendingMissions(): List<PendingMission>

    // 상시미션 조회
    @GET("api/missions/regular")
    suspend fun getRegularMissions(): List<Mission_Data>

    // 돌발미션 조회
    @GET("api/missions/event")
    suspend fun getEventMissions(): List<Mission_Data>

    // 미션 업로드
    @Multipart
    @POST("api/missions/{missionId}/submit")
    suspend fun submitMission(
        @Path("missionId") missionId: Int,
        @Part photo: MultipartBody.Part
    ): retrofit2.Response<SubmitMissionResponse>    // SubmitMissionResponse.kt 확인

    // 미션 상세 조회
    @GET("api/missions/{missionId}")
    suspend fun getMissionDetail(
        @Path("missionId") missionId: Int
    ): MissionDetailResponse


    // ADMIN
    // 승인 대기 미션 목록
    @GET("api/admin/missions/pending")
    suspend fun getPendingMissions_ADMIN(): List<Mission_Data>

    // 마감된 미션 목록
    @GET("api/admin/missions/deadLine")
    suspend fun getDeadLineMissions(): List<Mission_Data>

    // 종료된 미션 목록
    @GET("api/admin/missions/termination")
    suspend fun getTerminationMissions(): List<Mission_Data>

    @GET("api/missions/history")
    suspend fun getMissionHistory(): List<MissionHistoryItem>

    // 참여 이력 상세 조회
    @GET("api/missions/history/{participationId}")
    suspend fun getMissionHistoryDetail(
        @retrofit2.http.Path("participationId") participationId: Long
    ): MissionHistoryDetail
}