package com.example.myapplication.ui.MissionHistory

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.data.ApiClient
import com.example.myapplication.databinding.FragmentMissionHistoryDetailBinding
import kotlinx.coroutines.launch

class MissionHistoryDetailFragment : Fragment(R.layout.fragment_mission_history_detail) {

    private lateinit var binding: FragmentMissionHistoryDetailBinding
    private val args: MissionHistoryDetailFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMissionHistoryDetailBinding.bind(view)

        fetchDetail(args.participationId)
    }

    private fun fetchDetail(id: Long) {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getMissionsApi(requireContext())
                val data = api.getMissionHistoryDetail(id)

                // 1. 기본 정보 바인딩
                binding.detailTitle.text = data.title
                binding.detailContent.text = data.content
                binding.detailDate.text = "${data.startDate} ~ ${data.deadLine}"
                binding.detailPeople.text = "참가인원 ${data.participationCount}명"
                binding.detailPoint.text = "미션비 ${data.missionPoint}p"

                // 2. 이미지 로딩
                if (!data.iconUrl.isNullOrEmpty()) {
                    Glide.with(this@MissionHistoryDetailFragment).load(data.iconUrl).into(binding.detailIcon)
                }
                if (!data.submittedPhotoUrl.isNullOrEmpty()) {
                    Glide.with(this@MissionHistoryDetailFragment).load(data.submittedPhotoUrl).into(binding.detailSubmittedPhoto)
                }

                // 3. [핵심] 텍스트 박스 디자인 변경 (배경색 + 텍스트)
                // bg_capsule_white가 있으므로 이제 R.drawable.bg_capsule_white 에러가 사라집니다.
                binding.btnStatus.setBackgroundResource(R.drawable.bg_capsule_white)

                when (data.participationStatus) {
                    "APPROVED" -> {
                        binding.btnStatus.text = "승인 완료"
                        // 초록색 (#4CAF50)
                        binding.btnStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                    }
                    "PENDING" -> {
                        binding.btnStatus.text = "승인 대기"
                        // 노란색 (#FFC107)
                        binding.btnStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFC107"))
                    }
                    "REJECTED" -> {
                        binding.btnStatus.text = "승인 거절"
                        // 빨간색 (#F44336)
                        binding.btnStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F44336"))
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}