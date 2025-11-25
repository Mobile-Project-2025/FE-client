package com.example.myapplication.ui.Point

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RewardItem(
    val brand: String,
    val title: String,
    val price: Int,
    val category: String,
    val imageRes: Int
) : Parcelable