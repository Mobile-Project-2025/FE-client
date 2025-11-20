package com.example.myapplication.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    // 초기 포인트 10800점으로 설정
    private val _userPoints = MutableLiveData(10800)
    val userPoints: LiveData<Int> = _userPoints

    // 포인트 차감 함수 (성공하면 true, 실패하면 false 반환)
    fun deductPoints(price: Int): Boolean {
        val current = _userPoints.value ?: 0
        if (current >= price) {
            _userPoints.value = current - price
            return true
        }
        return false
    }
}