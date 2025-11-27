package com.example.myapplication.ui.Mission_Camera

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentMissionCameraBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

import com.example.myapplication.data.ApiClient
import retrofit2.HttpException

class Mission_CameraFragment : Fragment() {

    private var _binding: FragmentMissionCameraBinding? = null
    private val binding get() = _binding!!

    private var capturedBitmap: Bitmap? = null


    // 현재 사진이 있나 없나 판정
    private var IsCameraComplete = false


    // 카메라 촬영 런처
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            if (bitmap != null) {
                capturedBitmap = bitmap
                binding.missionCameraPreviewBeforeSubmit.setImageBitmap(bitmap)
                IsCameraComplete = true
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMissionCameraBinding.inflate(inflater, container, false)
        val root = binding.root

        // HomeFragment에서 넘어올 때, mission 상세 정보 받아오기
        val missionId = arguments?.getInt("missionId") ?: -1
        val title = arguments?.getString("title") ?: ""
        val content = arguments?.getString("content") ?: ""
        val missionPoint = arguments?.getInt("missionPoint") ?: 0
        val iconImageUrl = arguments?.getString("iconImageUrl") ?: ""
        val startDate = arguments?.getString("startDate") ?: ""
        val deadLine = arguments?.getString("deadLine") ?: ""
        val participationCount = arguments?.getInt("participationCount") ?: 0
        val hasSubmitted = arguments?.getBoolean("hasSubmitted") ?: false

        Log.d(
            "MissionCameraArgs",
            "missionId=$missionId, title=$title, missionPoint=$missionPoint, startDate=$startDate, deadLine=$deadLine, participationCount=$participationCount, hasSubmitted=$hasSubmitted"
        )

        // 제목
        binding.missionCameraName.text = title
        // 설명
        binding.missionCameraInfoContent.text = content
        // 포인트
        binding.missionCameraHowMuchPoint.text = "${missionPoint}P"

        // 기간(startDate ~ deadLine) - 날짜 문자열에서 T 앞부분만 사용
        val startFormatted = startDate.split("T").firstOrNull() ?: startDate
        val deadLineFormatted = deadLine.split("T").firstOrNull() ?: deadLine
        binding.missionCameraDuration.text = "기간 $startFormatted ~ $deadLineFormatted"

        // 참여 인원
        binding.missionCameraHowMuchPeople.text = "${participationCount}명"

        // 아이콘 이미지
        Glide.with(this)
            .load(iconImageUrl)
            .placeholder(R.drawable.ic_mission)
            .error(R.drawable.ic_mission)
            .into(binding.missionCameraImage)

        // 이미 제출(hasSubmitted == true) 한 경우, 참여하기 버튼 숨김
        if (hasSubmitted) {
            binding.missionCameraButtonParticipate.visibility = View.GONE
            binding.missionCameraPreviewBeforeSubmit.visibility = View.GONE
        } else {
            binding.missionCameraButtonParticipate.visibility = View.VISIBLE
            binding.missionCameraPreviewBeforeSubmit.visibility = View.VISIBLE
        }


        // ========================================================================
        // 회색 정사각형 클릭 시, 카메라 런처 실행
        binding.missionCameraPreviewBeforeSubmit.setOnClickListener {
            if (IsCameraComplete) {
                Toast.makeText(requireContext(), "아래의 참여하기 버튼을 눌러 업로드해주세요", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "아래의 참여하기 버튼을 눌러 촬영해주세요", Toast.LENGTH_SHORT).show()
            }
        }


        // "참여하기" 버튼 클릭 == 촬영 및 업로드
        binding.missionCameraButtonParticipate.setOnClickListener {
            // 촬영 완료된 상황 == 업로드
            if (IsCameraComplete && capturedBitmap != null) {
//                Toast.makeText(requireContext(), "업로드 했긔", Toast.LENGTH_SHORT).show()
                if (missionId == -1) {
                    Toast.makeText(requireContext(), "missionId가 없음", Toast.LENGTH_SHORT).show()
                } else {
                    uploadMissionImage(missionId, capturedBitmap!!)
                }

            // 촬영 안된 상황 == 카메라
            } else {
                cameraLauncher.launch(null)
            }
        }
        return root
    }


    // 업로드 함수
    private fun uploadMissionImage(missionId: Int, bitmap: Bitmap) {
        lifecycleScope.launch {
            try {
                // Bitmap -> JPEG byte array 변환
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
                val imageBytes = byteArrayOutputStream.toByteArray()

                val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageBytes)
                val photoPart = MultipartBody.Part.createFormData(
                    "photo",
                    "mission_${missionId}.jpg",
                    requestBody
                )

                // 네트워크는 IO 스레드에서 실행
                val response = withContext(Dispatchers.IO) {
                    ApiClient
                        .getMissionsApi(requireContext())
                        .submitMission(missionId, photoPart)
                }

                // submitMission의 리턴 타입이 retrofit2.Response<...> 인 경우에 대비한 처리
                if (response is retrofit2.Response<*>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "미션 업로드 성공!_1", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "업로드 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // 리턴 타입이 직접 DTO인 경우
                    Toast.makeText(requireContext(), "미션 업로드 성공!_2", Toast.LENGTH_SHORT).show()
                }
            } catch (e: HttpException) {
                Log.e("MissionCamera", "HTTP error: ${e.code()} ${e.message()}", e)
                Toast.makeText(requireContext(), "업로드 실패", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("MissionCamera", "uploadMissionImage error", e)
                Toast.makeText(requireContext(), "업로드 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}