package com.example.myapplication.ui.Admin_MissionList

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
import androidx.navigation.fragment.findNavController
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

    // 현재 선택된 필터
    private var currentFilter: MissionApiType = MissionApiType.PENDING

    // 어떤 목록을 보는지 구분용
    private enum class MissionApiType {
        PENDING,      // 승인 대기
        DEADLINE,     // 마감된
        TERMINATION,  // 종료
        ALL           // 전체
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminMissionListBinding.inflate(inflater, container, false)
        val root = binding.root

        // 최초 진입 시: 승인 대기 미션 목록
        currentFilter = MissionApiType.PENDING
        loadMissions(currentFilter)

        // 전체 버튼 (xml에 존재할 경우에만 동작)
        binding.adminMissionButtonTypeMissionAll?.setOnClickListener {
            currentFilter = MissionApiType.ALL
            loadMissions(currentFilter)
        }

        // 승인 대기
        binding.adminMissionButtonTypeMission1.setOnClickListener {
            currentFilter = MissionApiType.PENDING
            loadMissions(currentFilter)
        }

        // 마감된
        binding.adminMissionButtonTypeMission2.setOnClickListener {
            currentFilter = MissionApiType.DEADLINE
            loadMissions(currentFilter)
        }

        // 종료
        binding.adminMissionButtonTypeMission3.setOnClickListener {
            currentFilter = MissionApiType.TERMINATION
            loadMissions(currentFilter)
        }

        return root
    }

    // --------------------------
    // 1) 서버에서 목록 가져오기 (Retrofit 사용)
    // --------------------------
    private fun loadMissions(type: MissionApiType) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = ApiClient.getMissionsApi(requireContext())

                val missions: List<Mission_Data> = when (type) {
                    MissionApiType.PENDING -> api.getPendingMissions_ADMIN()
                    MissionApiType.DEADLINE -> api.getDeadLineMissions()
                    MissionApiType.TERMINATION -> api.getTerminationMissions()
                    // 전체 목록용 API가 아직 없다면, 임시로 승인 대기와 동일하게 사용
                    MissionApiType.ALL -> api.getPendingMissions_ADMIN()
                }

                Log.d(
                    "AdminMissionAPI",
                    "loadMissions($type) 성공, missions size = ${missions.size}"
                )

                // 디버깅용 전체 JSON 로그
                val gson = GsonBuilder().setPrettyPrinting().create()
                val missionsJson = gson.toJson(missions)
                Log.d("AdminMissionAPI_FULL_RESPONSE", missionsJson)

                renderMissions(type, missions)
            } catch (e: HttpException) {
                Log.e(
                    "AdminMissionAPI",
                    "loadMissions($type) 실패, HTTP code = ${e.code()}, message = ${e.message()}",
                    e
                )
                Toast.makeText(
                    requireContext(),
                    "미션 로딩 실패: ${e.code()}",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Throwable) {
                Log.e(
                    "AdminMissionAPI",
                    "loadMissions($type) 호출 중 기타 오류: ${e.localizedMessage}",
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

    // --------------------------
    // 2) 화면에 카드 렌더링
    // --------------------------
    private fun renderMissions(
        type: MissionApiType,
        missions: List<Mission_Data>
    ) {
        val missionContainer = binding.adminMissionListContainer
        missionContainer.removeAllViews()

        val inflater = LayoutInflater.from(requireContext())

        missions.forEach { mission ->
            val missionView = inflater.inflate(
                R.layout.layout_mission_box_mode,
                missionContainer,
                false
            )

            val icon = missionView.findViewById<ImageView>(R.id.home_mission_icon_mode)
            val title = missionView.findViewById<TextView>(R.id.home_mission_text_mode)
            val point = missionView.findViewById<TextView>(R.id.home_mission_text_point_mode)
            val people = missionView.findViewById<TextView>(R.id.home_mission_text_people_mode)

            title.text = mission.title
            point.text = "${mission.missionPoint}P"

            val peopleCount = mission.participationCount ?: 0
            people.text = "${peopleCount}명"

            val url = mission.iconImageUrl
            if (!url.isNullOrBlank()) {
                Glide.with(missionView)
                    .load(url)
                    .placeholder(R.drawable.ic_mission)
                    .error(R.drawable.ic_mission)
                    .into(icon)
            } else {
                icon.setImageResource(R.drawable.ic_mission)
            }

            // 승인 대기 목록일 때만 상세 화면 이동
            if (type == MissionApiType.PENDING) {
                missionView.setOnClickListener {
                    val bundle = Bundle().apply {
                        putLong("missionId", mission.missionId ?: -1L)
                        putString("title", mission.title)
                        putInt("missionPoint", mission.missionPoint ?: 0)
                        putString("createdAt", mission.createdAt)
                        putString("iconImageUrl", mission.iconImageUrl)
                    }
                    findNavController().navigate(
                        R.id.move_admin_mission_list_to_pending_detail,
                        bundle
                    )
                }
            }

            missionContainer.addView(missionView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
