package com.example.myapplication.ui.Mission_Camera

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class Mission_CameraFragment : Fragment() {

    private var _binding: FragmentMissionCameraBinding? = null
    private val binding get() = _binding!!
    private var Button_Participate = false  // 참여하기 버튼의 기능을 [촬영 ==> 제출] 로 바꾸기 위한 트리거

    // 🔥 카메라 촬영 런처
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            if (bitmap != null) {
                binding.missionCameraPreviewBeforeSubmit.setImageBitmap(bitmap)


                // 업로드 함수 호출 == 추후 구현 예정
//                uploadMissionImage(bitmap)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMissionCameraBinding.inflate(inflater, container, false)
        val root = binding.root

        // HomeFragment에서 넘어올 때, mission 관련 정보 받아오기
        val title = arguments?.getString("title") ?: ""
        val missionPoint = arguments?.getInt("missionPoint") ?: 0
        val createdAt = arguments?.getString("createdAt") ?: ""
        val iconImageUrl = arguments?.getString("iconImageUrl") ?: ""

        Log.d("MissionCameraArgs", "title=$title, missionPoint=$missionPoint, createdAt=$createdAt, iconImageUrl=$iconImageUrl")

        binding.missionCameraName.text = title
        binding.missionCameraHowMuchPoint.text = "Point ${missionPoint}p"

        val dateFormatted = createdAt.split("T").firstOrNull() ?: createdAt
        binding.missionCameraDuration.text = "기간 $dateFormatted"    // 이거 시작기간인데 수정해야할듯?

        Glide.with(this)
            .load(iconImageUrl)
            .placeholder(R.drawable.ic_mission)
            .error(R.drawable.ic_mission)
            .into(binding.missionCameraImage)


        // "참여하기" 버튼 클릭 == 촬영
        binding.missionCameraButtonParticipate.setOnClickListener {
            if (Button_Participate == false) {
                cameraLauncher.launch(null)
            } else {
                Button_Participate = true
            }
        }
        return root
    }


    // 업로드 함수
//    private fun uploadMissionImage(bitmap: Bitmap) {
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}