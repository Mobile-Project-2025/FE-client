package com.example.myapplication.ui.Admin_MissionList

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentAdminPendingMissionDetailBinding
import com.example.myapplication.ui.Log_in.HttpClientProvider
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class AdminPendingMissionDetailFragment : Fragment() {

    private var _binding: FragmentAdminPendingMissionDetailBinding? = null
    private val binding get() = _binding!!

    private val baseUrl = "http://43.202.225.195:8080"

    // 전달받은 미션 ID
    private var missionId: Long = -1L

    // 서버에서 상세 조회로 받은 미션 정보
    private var missionTitle: String = ""
    private var missionContent: String = ""
    private var missionIconUrl: String = ""
    private var missionPoint: Int = 0
    private var missionStartDate: String = ""
    private var missionDeadLine: String = ""
    private var missionParticipationCount: Int = 0

    // 참가작 목록 보관
    private val participationList = mutableListOf<ParticipationDto>()
    private var selectedParticipationId: Long? = null
    private var selectedView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminPendingMissionDetailBinding.inflate(inflater, container, false)
        val root = binding.root

        // arguments에서 미션 ID 받기 (목록에서 넘겨준 값)
        arguments?.let { args ->
            missionId = args.getLong("missionId", -1L)
        }

        if (missionId <= 0) {
            Toast.makeText(requireContext(), "잘못된 미션 ID 입니다.", Toast.LENGTH_SHORT).show()
        } else {
            // 미션 상세 + 참가작 목록 한번에 조회
            loadMissionDetailAndParticipation()
        }

        // 승인/반려 버튼
        binding.btnApprove.setOnClickListener {
            val targetId = selectedParticipationId
            if (targetId == null) {
                Toast.makeText(requireContext(), "승인/반려할 참가작을 선택해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                showConfirmDialog(targetId)
            }
        }

        return root
    }

    // 헤더(아이콘, 제목, 기간, 포인트, 설명) 세팅
    private fun initHeaderUi() {

        // 아이콘
        if (missionIconUrl.isNotBlank()) {
            Glide.with(this)
                .load(missionIconUrl)
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(binding.ivMissionIcon)
        } else {
            binding.ivMissionIcon.setImageResource(R.drawable.default_profile)
        }

        // 제목
        binding.tvMissionTitle.text = missionTitle

        // 기간 + 포인트
        val dateText = if (missionStartDate.isNotBlank() && missionDeadLine.isNotBlank()) {
            "$missionStartDate ~ $missionDeadLine"
        } else {
            ""
        }
        val meta = if (dateText.isNotBlank()) {
            "$dateText  ·  ${missionPoint}P"
        } else {
            "${missionPoint}P"
        }
        binding.tvMissionMeta.text = meta

        // 설명
        if (missionContent.isNotBlank()) {
            binding.tvMissionDesc.text = missionContent
        } else {
            binding.tvMissionDesc.text = "이 미션의 상세 설명이 없습니다."
        }
    }

    // 1) 미션 상세 + 2) 참가작 목록 한 번에 불러오기
    private fun loadMissionDetailAndParticipation() {
        val client = HttpClientProvider.get(requireContext())

        // GET /api/admin/missions/request/{missionId}
        val path = "/api/admin/missions/request/$missionId"

        val request = Request.Builder()
            .url(baseUrl + path)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "미션 상세 불러오기 실패: ${e.localizedMessage}",
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
                            "미션 상세 불러오기 실패: code=${response.code}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return
                }

                try {
                    val obj = JSONObject(body)

                    // ------- 미션 기본 정보 파싱 -------
                    missionTitle = obj.optString("title")
                    missionContent = obj.optString("content")
                    missionIconUrl = obj.optString("iconImageUrl")
                    missionPoint = obj.optInt("missionPoint")
                    missionStartDate = obj.optString("startDate")
                    missionDeadLine = obj.optString("deadLine")
                    missionParticipationCount = obj.optInt("participationCount")

                    // ------- 참가작 리스트 파싱 -------
                    val requesterArray: JSONArray = obj.optJSONArray("requesterList") ?: JSONArray()
                    val list = mutableListOf<ParticipationDto>()

                    for (i in 0 until requesterArray.length()) {
                        val item = requesterArray.getJSONObject(i)
                        val dto = ParticipationDto(
                            participationId = item.optLong("participationId"),
                            nickname = item.optString("nickName"),
                            photoUrl = item.optString("participationPhoto"),
                            participationTime = item.optString("missionParticipationTime")
                        )
                        list.add(dto)
                    }

                    requireActivity().runOnUiThread {
                        participationList.clear()
                        participationList.addAll(list)

                        // 헤더 갱신
                        initHeaderUi()
                        // 참가작 카드 갱신
                        renderParticipationList()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "응답 파싱 중 오류가 발생했습니다.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }

    // 참가작 카드들 그리기
    private fun renderParticipationList() {
        val container = binding.layoutParticipationContainer
        container.removeAllViews()

        if (participationList.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            return
        } else {
            binding.tvEmpty.visibility = View.GONE
        }

        val inflater = LayoutInflater.from(requireContext())

        participationList.forEach { dto ->
            val itemView = inflater.inflate(
                R.layout.item_admin_participation,
                container,
                false
            )

            val root = itemView.findViewById<View>(R.id.layout_participation_root)
            val iv = itemView.findViewById<ImageView>(R.id.iv_participation_image)
            val tv = itemView.findViewById<TextView>(R.id.tv_participation_name)

            // 닉네임
            tv.text = dto.nickname

            // 이미지
            if (dto.photoUrl.isNotBlank()) {
                Glide.with(itemView)
                    .load(dto.photoUrl)
                    .placeholder(R.drawable.horse_picture)
                    .error(R.drawable.horse_picture)
                    .into(iv)
            } else {
                iv.setImageResource(R.drawable.horse_picture)
            }

            // 카드 클릭 시 선택 상태 변경
            root.setOnClickListener {
                selectedParticipationId = dto.participationId
                selectedView?.isSelected = false
                root.isSelected = true
                selectedView = root
                highlightSelection()
            }

            container.addView(itemView)
        }

        selectedParticipationId = null
        selectedView = null
        highlightSelection()
    }

    // 선택된 참가작만 배경색 표시
    private fun highlightSelection() {
        val container = binding.layoutParticipationContainer
        for (i in 0 until container.childCount) {
            val itemRoot = container.getChildAt(i)
                .findViewById<View>(R.id.layout_participation_root)
            if (itemRoot == selectedView) {
                itemRoot.setBackgroundColor(0xFFE0F2F1.toInt()) // 약한 민트
            } else {
                itemRoot.setBackgroundColor(0x00000000) // 투명
            }
        }
    }

    // 승인/반려 확인 모달
    private fun showConfirmDialog(participationId: Long) {
        AlertDialog.Builder(requireContext())
            .setTitle("정말로 승인/반려하시겠습니까?")
            .setMessage("확인 이후에는 승인/반려를 되돌릴 수 없습니다.\n선택한 참가작에 대해 진행할 작업을 골라주세요.")
            .setNegativeButton("반려하기") { _, _ ->
                requestApproveReject(participationId, approve = false)
            }
            .setPositiveButton("승인하기") { _, _ ->
                requestApproveReject(participationId, approve = true)
            }
            .show()
    }

    // PATCH /approve or /reject 호출
    private fun requestApproveReject(participationId: Long, approve: Boolean) {
        val client = HttpClientProvider.get(requireContext())

        val path = if (approve) {
            "/api/admin/missions/request/$participationId/approve"
        } else {
            "/api/admin/missions/request/$participationId/reject"
        }

        val emptyBody = "".toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(baseUrl + path)
            .patch(emptyBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "처리 실패: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val ok = response.isSuccessful

                requireActivity().runOnUiThread {
                    if (!ok) {
                        Toast.makeText(
                            requireContext(),
                            "처리 실패: code=${response.code}",
                            Toast.LENGTH_LONG
                        ).show()
                        return@runOnUiThread
                    }

                    // 승인/반려된 참가작을 리스트에서 제거
                    val index = participationList.indexOfFirst {
                        it.participationId == participationId
                    }
                    if (index != -1) {
                        participationList.removeAt(index)
                        binding.layoutParticipationContainer.removeViewAt(index)
                    }

                    selectedParticipationId = null
                    selectedView = null
                    highlightSelection()

                    val msg = if (approve) "승인되었습니다." else "반려되었습니다."
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

                    // 더 이상 남은 참가작이 없으면 뒤로가기
                    if (participationList.isEmpty()) {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class ParticipationDto(
        val participationId: Long,
        val nickname: String,
        val photoUrl: String,
        val participationTime: String
    )
}
