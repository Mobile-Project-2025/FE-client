package com.example.myapplication.ui.Home

data class MissionItem(
    val title: String,
    val point: Int,
    val people: Int,
    val type: String // "승인대기", "상시", "돌발" 등 구분용
)