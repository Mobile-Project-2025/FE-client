package com.example.myapplication.data


// 미션 데이터 (swagger 기반)
data class Mission_Data(
    val missionId: Long,
    val title: String,
    val missionPoint: Int,
    val category: String,
    val iconImageUrl: String,
    val bannerImageUrl: String?,
    val participationCount: Int?,
    val createdAt: String,
    val participationId: Long? = null
)