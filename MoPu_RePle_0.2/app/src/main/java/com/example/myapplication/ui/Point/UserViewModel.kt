package com.example.myapplication.ui

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ApiClient
import com.example.myapplication.data.PointRequest
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    // 1. 유저 ID
    private val _userId = MutableLiveData<Long>()
    val userId: LiveData<Long> = _userId

    // 2. 포인트
    private val _userPoints = MutableLiveData<Int>(0)
    val userPoints: LiveData<Int> = _userPoints

    // 3. 닉네임
    private val _userNickname = MutableLiveData<String>("")
    val userNickname: LiveData<String> = _userNickname

    // 4. 구매 성공 이벤트
    private val _purchaseSuccess = MutableLiveData<Boolean>()
    val purchaseSuccess: LiveData<Boolean> = _purchaseSuccess


    // 1. 내 정보 가져오기 (초기화)
    fun fetchUserInfo() {
        viewModelScope.launch {
            try {
                val api = ApiClient.getUserApi(getApplication())
                val userData = api.getUserProfile()

                // 받아온 데이터 저장 (내부 변수 _ 에 저장)
                _userId.value = userData.userId
                _userNickname.value = userData.nickname
                _userPoints.value = userData.cumulativePoint


            } catch (e: Exception) {
                Log.e("UserViewModel", "정보 로드 실패", e)
            }
        }
    }

    // 2. [핵심] 아이템 구매 시 포인트 차감 및 서버 전송
    fun purchaseItem(price: Int) {
        val currentPoint = _userPoints.value ?: 0 // _userPoints 사용
        val myId = _userId.value

        // 예외처리: 유저 ID가 없거나 잔액 부족 시
        if (myId == null || currentPoint < price) {
            Toast.makeText(getApplication(), "오류: 정보를 불러오지 못했거나 잔액이 부족합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 1) 차감된 포인트 계산
        val newPoint = currentPoint - price

        viewModelScope.launch {
            try {
                // 2) 서버로 PATCH 요청 보내기
                val api = ApiClient.getUserApi(getApplication())
                val requestBody = PointRequest(point = newPoint)

                // [수정됨] 결과를 변수에 담지 않고 호출만 수행 (성공하면 다음 줄로 넘어감)
                api.updateUserPoint(myId, requestBody)

                // 3) 성공 시: 프론트엔드 포인트 즉시 갱신
                _userPoints.value = newPoint

                // 화면에 "성공했어요" 신호 보내기
                _purchaseSuccess.value = true
                _purchaseSuccess.value = false

            } catch (e: Exception) {
                Log.e("UserViewModel", "포인트 업데이트 실패", e)
                Toast.makeText(getApplication(), "서버 통신 실패: 구매가 취소되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}