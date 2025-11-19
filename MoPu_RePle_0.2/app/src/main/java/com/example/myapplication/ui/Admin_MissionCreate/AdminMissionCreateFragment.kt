package com.example.myapplication.ui.admin

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentAdminMissionCreateBinding
import com.example.myapplication.ui.Log_in.HttpClientProvider
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.Calendar
import android.content.res.ColorStateList

class AdminMissionCreateFragment : Fragment() {

    private var _binding: FragmentAdminMissionCreateBinding? = null
    private val binding get() = _binding!!

    // 선택된 카테고리 / 타입
    private var selectedCategory: String? = null
    private var selectedType: MissionType? = null

    private enum class MissionType {
        REGULAR, EVENT
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminMissionCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCategoryButtons()
        initMissionTypeRadioGroup()
        initDatePickers()
        initTextWatchers()

        // 미리보기
        binding.btnPreview.setOnClickListener { showPreviewDialog() }

        // 등록하기
        binding.btnSubmit.setOnClickListener {
            when (selectedType) {
                MissionType.REGULAR -> postRegularMission()
                MissionType.EVENT -> postEventMission()
                null -> Toast.makeText(requireContext(), "미션 유형을 선택하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --------------------------------------------------------------------
    // UI 초기화 영역
    // --------------------------------------------------------------------

    // 카테고리 버튼 4개 → 서버 ENUM 값 + 색상 처리
    private fun initCategoryButtons() {
        val buttons = listOf(
            binding.btnCategoryDump,
            binding.btnCategoryEtc,
            binding.btnCategoryTransport,
            binding.btnCategoryRecycle
        )

        // 서버에서 사용하는 ENUM 값 매핑
        val categoryMap: Map<Button, String> = mapOf(
            binding.btnCategoryDump to "TUMBLER",
            binding.btnCategoryEtc to "ETC",
            binding.btnCategoryTransport to "PUBLIC_TRANSPORTATION",
            binding.btnCategoryRecycle to "RECYCLE"
        )

        buttons.forEach { btn ->
            btn.isSelected = false
            btn.setTextColor(Color.parseColor("#000000"))

            btn.setOnClickListener {
                // 전체 버튼 unselect (흰 배경 / 검정 글자)
                buttons.forEach {
                    it.isSelected = false
                    it.setTextColor(Color.parseColor("#000000"))
                }

                // 클릭된 버튼만 selected (selector + 흰 글자)
                btn.isSelected = true
                btn.setTextColor(Color.parseColor("#FFFFFF"))

                selectedCategory = categoryMap[btn]
                updateSubmitEnabled()
            }
        }
    }

    // 상시 / 돌발 라디오 버튼 + 기간 필드 활성/비활성
    private fun initMissionTypeRadioGroup() {
        binding.rgType.setOnCheckedChangeListener { _, checkedId ->
            selectedType = when (checkedId) {
                R.id.rb_regular -> MissionType.REGULAR
                R.id.rb_event -> MissionType.EVENT
                else -> null
            }

            when (selectedType) {
                MissionType.REGULAR -> {
                    // 상시: 기간 비활성 + 값 비우기
                    binding.etStartDate.isEnabled = false
                    binding.etEndDate.isEnabled = false
                    binding.etStartDate.alpha = 0.4f
                    binding.etEndDate.alpha = 0.4f
                    binding.etStartDate.text?.clear()
                    binding.etEndDate.text?.clear()
                }
                MissionType.EVENT -> {
                    // 돌발: 기간 필수
                    binding.etStartDate.isEnabled = true
                    binding.etEndDate.isEnabled = true
                    binding.etStartDate.alpha = 1f
                    binding.etEndDate.alpha = 1f
                }
                null -> {
                    binding.etStartDate.isEnabled = true
                    binding.etEndDate.isEnabled = true
                    binding.etStartDate.alpha = 1f
                    binding.etEndDate.alpha = 1f
                }
            }

            updateSubmitEnabled()
        }
    }

    // 날짜 선택: 클릭 시 DatePickerDialog, 오늘 이후만 선택 가능
    private fun initDatePickers() {
        // 입력 칸 직접 타이핑 막고, 클릭으로만 선택하도록
        listOf(binding.etStartDate, binding.etEndDate).forEach { et ->
            et.isFocusable = false
            et.isClickable = true
        }

        binding.etStartDate.setOnClickListener {
            showDatePicker(binding.etStartDate)
        }
        binding.etEndDate.setOnClickListener {
            showDatePicker(binding.etEndDate)
        }
    }

    private fun showDatePicker(target: android.widget.EditText) {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                val mm = String.format("%02d", m + 1)
                val dd = String.format("%02d", d)
                val dateString = "$y-$mm-$dd"
                target.setText(dateString)
                updateSubmitEnabled()
            },
            year, month, day
        )

        // 오늘 이후만 선택 가능
        dialog.datePicker.minDate = cal.timeInMillis
        dialog.show()
    }

    // 텍스트 변경 감지 → 버튼 활성화 갱신
    private fun initTextWatchers() {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateSubmitEnabled()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.etTitle.addTextChangedListener(watcher)
        binding.etPoint.addTextChangedListener(watcher)
        binding.etContent.addTextChangedListener(watcher)
        binding.etStartDate.addTextChangedListener(watcher)
        binding.etEndDate.addTextChangedListener(watcher)
    }

    // --------------------------------------------------------------------
    // 공통 유틸
    // --------------------------------------------------------------------

    private fun updateSubmitEnabled() {
        val title = binding.etTitle.text.toString().trim()
        val point = binding.etPoint.text.toString().trim()
        val content = binding.etContent.text.toString().trim()

        val baseOk = title.isNotEmpty() &&
                point.isNotEmpty() &&
                content.isNotEmpty() &&
                selectedCategory != null &&
                selectedType != null

        val extraOk = when (selectedType) {
            MissionType.REGULAR -> true
            MissionType.EVENT -> {
                val start = binding.etStartDate.text.toString().trim()
                val end = binding.etEndDate.text.toString().trim()
                start.isNotEmpty() && end.isNotEmpty()
            }
            null -> false
        }

        val enabled = baseOk && extraOk

        // 실제 활성화 여부
        binding.btnSubmit.isEnabled = enabled

        // 보이는 색도 같이 변경
        if (enabled) {
            // 활성 상태: 검정 배경 + 흰 글자 (원하는 색으로 바꿔도 됨)
            binding.btnSubmit.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#000000"))
            binding.btnSubmit.setTextColor(Color.parseColor("#FFFFFF"))
        } else {
            // 비활성 상태: 기존처럼 회색
            binding.btnSubmit.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#E0E0E0"))
            binding.btnSubmit.setTextColor(Color.parseColor("#888888"))
        }
    }


    private fun showPreviewDialog() {
        val title = binding.etTitle.text.toString()
        val point = binding.etPoint.text.toString()
        val content = binding.etContent.text.toString()
        val category = selectedCategory ?: "-"
        val typeText = when (selectedType) {
            MissionType.REGULAR -> "상시 미션"
            MissionType.EVENT -> "돌발 미션"
            null -> "-"
        }
        val start = binding.etStartDate.text.toString()
        val end = binding.etEndDate.text.toString()

        val msg = buildString {
            appendLine("제목: $title")
            appendLine("카테고리: $category")
            appendLine("유형: $typeText")
            if (selectedType == MissionType.EVENT) {
                appendLine("기간: $start ~ $end")
            }
            appendLine("포인트: $point p")
            appendLine("설명: $content")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("미션 미리보기")
            .setMessage(msg)
            .setPositiveButton("확인", null)
            .show()
    }

    // --------------------------------------------------------------------
    // 서버 통신
    // --------------------------------------------------------------------

    // 상시 미션: JSON
    private fun postRegularMission() {
        val pointInt = binding.etPoint.text.toString().trim().toIntOrNull()
        if (pointInt == null) {
            Toast.makeText(requireContext(), "포인트를 올바른 숫자로 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val client = HttpClientProvider.get(requireContext())

        val json = JSONObject().apply {
            put("title", binding.etTitle.text.toString().trim())
            put("point", pointInt)
            put("content", binding.etContent.text.toString().trim())
            put("category", selectedCategory)
        }

        val body = json.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("http://43.202.225.195:8080/api/admin/missions/regular")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "상시 미션 생성 실패: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val resBody = response.body?.string() ?: ""
                Log.e("MISSION_REGULAR", "code=${response.code}, body=$resBody")

                requireActivity().runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "상시 미션 생성 완료", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.navigation_admin_mission_list)
                    } else {
                        AlertDialog.Builder(requireContext())
                            .setTitle("상시 미션 생성 실패")
                            .setMessage("code=${response.code}\n\n$resBody")
                            .setPositiveButton("확인", null)
                            .show()
                    }
                }
            }
        })
    }

    // 돌발 미션: multipart (이미지 없이, 기간 포함)
    private fun postEventMission() {
        val pointInt = binding.etPoint.text.toString().trim().toIntOrNull()
        if (pointInt == null) {
            Toast.makeText(requireContext(), "포인트를 올바른 숫자로 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val client = HttpClientProvider.get(requireContext())

        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()
        val category = selectedCategory ?: ""
        val start = binding.etStartDate.text.toString().trim()
        val end = binding.etEndDate.text.toString().trim()

        val builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("title", title)
            .addFormDataPart("point", pointInt.toString())
            .addFormDataPart("content", content)
            .addFormDataPart("category", category)
            .addFormDataPart("startDate", start)
            .addFormDataPart("deadLine", end)

        val request = Request.Builder()
            .url("http://43.202.225.195:8080/api/admin/missions/event")
            .post(builder.build())
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "돌발 미션 생성 실패: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val resBody = response.body?.string() ?: ""
                Log.e("MISSION_EVENT", "code=${response.code}, body=$resBody")

                requireActivity().runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "돌발 미션 생성 완료", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.navigation_admin_mission_list)
                    } else {
                        AlertDialog.Builder(requireContext())
                            .setTitle("돌발 미션 생성 실패")
                            .setMessage("code=${response.code}\n\n$resBody")
                            .setPositiveButton("확인", null)
                            .show()
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
