package com.example.myapplication.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// 리스트 조회용 아이템
data class MissionHistoryItem(
    val participationId: Long,
    val missionId: Long,
    val title: String,
    val bannerUrl: String?,
    val iconUrl: String?,
    val missionPoint: Int,
    val participationCount: Int,
    val participationStatus: String, // "APPROVED", "REJECTED", "PENDING"
    val participatedAt: String
)

// 상세 조회용 아이템
data class MissionHistoryDetail(
    val participationId: Long,
    val missionId: Long,
    val title: String,
    val content: String,
    val bannerUrl: String?,
    val iconUrl: String?,
    val missionPoint: Int,
    val participationCount: Int,
    val category: String,
    val missionType: String,
    val participationStatus: String,
    val submittedPhotoUrl: String?,
    val participatedAt: String,
    val startDate: String,
    val deadLine: String
)