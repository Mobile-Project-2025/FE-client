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
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentAdminMissionListBinding
import com.example.myapplication.ui.Log_in.HttpClientProvider
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class AdminMissionListFragment : Fragment() {

    private var _binding: FragmentAdminMissionListBinding? = null
    private val binding get() = _binding!!

    private val baseUrl = "http://43.202.225.195:8080"

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

        // 최초 진입 시: 승인 대기 미션 목록 보여주기
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
    // 1) 서버에서 목록 가져오기
    // --------------------------
    private fun loadMissions(type: MissionApiType) {
        val client = HttpClientProvider.get(requireContext())

        val path = when (type) {
            MissionApiType.PENDING     -> "/api/admin/missions/pending"
            MissionApiType.DEADLINE    -> "/api/admin/missions/deadLine"
            MissionApiType.TERMINATION -> "/api/admin/missions/termination"
            MissionApiType.ALL         -> "/api/admin/missions"
        }

        val request = Request.Builder()
            .url(baseUrl + path)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "미션 목록 불러오기 실패: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "미션 목록 불러오기 실패: code=${response.code}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return
                }

                val missions = parseMissionList(body)

                Log.d("AdminMissionAPI", "loadMissions($type) size=${missions.size}")

                requireActivity().runOnUiThread {
                    renderMissions(type, missions)
                }
            }
        })
    }

    // --------------------------
    // 2) JSON → 데이터 객체 변환
    // --------------------------
    private fun parseMissionList(jsonString: String): List<AdminMissionDto> {
        val list = mutableListOf<AdminMissionDto>()

        try {
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val obj: JSONObject = jsonArray.getJSONObject(i)

                val dto = AdminMissionDto(
                    missionId = obj.optLong("missionId"),
                    title = obj.optString("title"),
                    missionPoint = obj.optInt("missionPoint"),
                    category = obj.optString("category"),
                    iconImageUrl = obj.optString("iconImageUrl", ""),
                    bannerImageUrl = obj.optString("bannerImageUrl", ""),
                    participationCount = obj.optInt("participationCount"),
                    createdAt = obj.optString("createdAt")
                )

                list.add(dto)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return list
    }

    // --------------------------
    // 3) 화면에 카드 렌더링
    // --------------------------
    private fun renderMissions(
        type: MissionApiType,
        missions: List<AdminMissionDto>
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

            // 현재 layout_mission_box_mode.xml 기준 id
            val icon = missionView.findViewById<ImageView>(R.id.home_mission_icon_mode)
            val title = missionView.findViewById<TextView>(R.id.home_mission_text_mode)
            val point = missionView.findViewById<TextView>(R.id.home_mission_text_point_mode)
            val people = missionView.findViewById<TextView>(R.id.home_mission_text_people_mode)

            title.text = mission.title
            point.text = "${mission.missionPoint}P"
            people.text = "${mission.participationCount}명"

            val url = mission.iconImageUrl
            if (url.isNotBlank()) {
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
                        putLong("missionId", mission.missionId)
                        putString("title", mission.title)
                        putInt("missionPoint", mission.missionPoint)
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

    // --------------------------
    // 4) DTO 정의
    // --------------------------
    data class AdminMissionDto(
        val missionId: Long,
        val title: String,
        val missionPoint: Int,
        val category: String,
        val iconImageUrl: String,
        val bannerImageUrl: String,
        val participationCount: Int,
        val createdAt: String
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
