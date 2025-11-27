package com.example.myapplication.data

data class MissionDetailResponse(
    val missionId: Int,
    val title: String,
    val content: String,
    val missionPoint: Int,
    val category: String,
    val iconImageUrl: String,
    val bannerImageUrl: String?,
    val startDate: String,
    val deadLine: String,
    val missionType: String,
    val status: String,
    val participationCount: Int?,
    val hasSubmitted: Boolean
)