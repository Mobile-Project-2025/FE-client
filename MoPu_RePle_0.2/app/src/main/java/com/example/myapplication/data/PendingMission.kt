package com.example.myapplication.data

data class PendingMission(
    val participationId: Long,
    val missionId: Long,
    val title: String,
    val missionPoint: Int,
    val category: String,
    val iconImageUrl: String,
    val missionType: String,
    val participationStatus: String,
    val submittedPhotoUrl: String,
    val submittedAt: String
)