package com.example.myapplication.ui.Admin_MissionList // <- 네 실제 패키지명에 맞게 수정

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.data.ApiClient
import com.example.myapplication.data.Mission_Data
import com.example.myapplication.databinding.FragmentAdminMissionListBinding
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AdminMissionListFragment : Fragment() {

    private var _binding: FragmentAdminMissionListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminMissionListBinding.inflate(inflater, container, false)
        val root = binding.root

        // 미션 박스 컨테이너
        val missionContainer = binding.adminMissionListContainer

        // HomeFragment와 동일한 방식으로 미션 박스 그리는 함수
        fun renderMissions(missions: List<Mission_Data>) {
            missionContainer.removeAllViews()

            missions.forEach { mission ->
                val missionView = inflater.inflate(
                    R.layout.layout_mission_box_mode,
                    missionContainer,
                    false
                )

                val title =
                    missionView.findViewById<TextView>(R.id.home_mission_text_mode)
                val point =
                    missionView.findViewById<TextView>(R.id.home_mission_text_point_mode)
                val people =
                    missionView.findViewById<TextView>(R.id.home_mission_text_people_mode)
                val icon =
                    missionView.findViewById<ImageView>(R.id.home_mission_icon_mode)

                // title, point
                title.text = mission.title
                point.text = "${mission.missionPoint}P"

                // people (참여 인원; 없으면 0으로 처리)
                val peopleCount = mission.participationCount ?: 0
                people.text = "${peopleCount}명"

                // icon
                Glide.with(missionView)
                    .load(mission.iconImageUrl)
                    .placeholder(R.drawable.ic_mission)
                    .error(R.drawable.ic_mission)
                    .into(icon)

                // (관리자 페이지에서 클릭 동작은 추후 구현)
                missionContainer.addView(missionView)
            }
        }

        // ================================
        // 버튼 클릭 시 관리자용 미션 목록 불러오기
        // ================================

        // 1. "승인 대기 미션" 버튼 → /api/admin/missions/pending
        binding.adminMissionButtonTypeMission1.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val api = ApiClient.getMissionsApi(requireContext())
                    val missions = api.getPendingMissions_ADMIN()
                    Log.d("AdminMissionAPI", "getPendingMissions 성공, missions size = ${missions.size}")

                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val missionsJson = gson.toJson(missions)
                    Log.d("AdminMissionAPI_FULL_RESPONSE", missionsJson)

                    renderMissions(missions)
                } catch (e: HttpException) {
                    Log.e(
                        "AdminMissionAPI",
                        "getPendingMissions 실패, HTTP code = ${e.code()}, message = ${e.message()}",
                        e
                    )
                    Toast.makeText(
                        requireContext(),
                        "승인 대기 미션 로딩 실패: ${e.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Throwable) {
                    Log.e(
                        "AdminMissionAPI",
                        "getPendingMissions 호출 중 기타 오류: ${e.localizedMessage}",
                        e
                    )
                    Toast.makeText(
                        requireContext(),
                        "에러: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // 2. "마감된 미션" 버튼 → /api/admin/missions/deadLine
        binding.adminMissionButtonTypeMission2.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val api = ApiClient.getMissionsApi(requireContext())
                    val missions = api.getDeadLineMissions()
                    Log.d("AdminMissionAPI", "getDeadLineMissions 성공, missions size = ${missions.size}")

                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val missionsJson = gson.toJson(missions)
                    Log.d("AdminMissionAPI_FULL_RESPONSE", missionsJson)

                    renderMissions(missions)
                } catch (e: HttpException) {
                    Log.e(
                        "AdminMissionAPI",
                        "getDeadLineMissions 실패, HTTP code = ${e.code()}, message = ${e.message()}",
                        e
                    )
                    Toast.makeText(
                        requireContext(),
                        "마감된 미션 로딩 실패: ${e.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Throwable) {
                    Log.e(
                        "AdminMissionAPI",
                        "getDeadLineMissions 호출 중 기타 오류: ${e.localizedMessage}",
                        e
                    )
                    Toast.makeText(
                        requireContext(),
                        "에러: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // 3. "종료 미션" 버튼 → /api/admin/missions/termination
        binding.adminMissionButtonTypeMission3.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val api = ApiClient.getMissionsApi(requireContext())
                    val missions = api.getTerminationMissions()
                    Log.d("AdminMissionAPI", "getTerminationMissions 성공, missions size = ${missions.size}")

                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val missionsJson = gson.toJson(missions)
                    Log.d("AdminMissionAPI_FULL_RESPONSE", missionsJson)

                    renderMissions(missions)
                } catch (e: HttpException) {
                    Log.e(
                        "AdminMissionAPI",
                        "getTerminationMissions 실패, HTTP code = ${e.code()}, message = ${e.message()}",
                        e
                    )
                    Toast.makeText(
                        requireContext(),
                        "종료 미션 로딩 실패: ${e.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Throwable) {
                    Log.e(
                        "AdminMissionAPI",
                        "getTerminationMissions 호출 중 기타 오류: ${e.localizedMessage}",
                        e
                    )
                    Toast.makeText(
                        requireContext(),
                        "에러: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}