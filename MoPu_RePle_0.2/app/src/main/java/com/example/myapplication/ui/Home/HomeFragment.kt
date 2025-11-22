package com.example.myapplication.ui.Home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.data.ApiClient
import com.example.myapplication.data.Mission_Data
import com.example.myapplication.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.google.gson.Gson
import com.google.gson.GsonBuilder

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 이미지 좌우 슬라이드 영역
        val pager = root.findViewById<ViewPager2>(R.id.home_img_slide_1)
        val sliderImages = listOf(
            R.drawable.horse_picture,
            R.drawable.horse_picture,
            R.drawable.horse_picture
        )
        pager.adapter = ImagePagerAdapter(sliderImages)


// 구버전
//        // 미션 관련 부분
//        val missionContainer = binding.homeMissionListContainer
//        // 미션 데이터 2차원 배열
//        val mission_content = arrayOf(
//            //
//            // 6개 정보 배열
//            // ID | 고정 미션 여부 | 승인 대기 여부 | 이름 | 포인트 | 인원
//            //
//            // 승인대기 여부의 경우
//            // 0 == 생성은 되었으나, 참여는 아직 안함
//            // 1 == 참여는 했고, 승인 대기 중
//            // 2 == 기간이 지나서 마감됨
//            // 3 == 관리자가 종료 처리함
//            arrayOf("1", "1", "0", "페트병 버리기", "300", "10"),
//            arrayOf("2", "1", "0", "분리수거", "200", "5"),
//            arrayOf("3", "0", "1", "이거 승인 대기 중임", "67", "15"),
//            arrayOf("4", "0", "2", "이거 기간 지나서 마감", "49", "25"),
//            arrayOf("5", "0", "3", "이거 종료 처리한 거임", "999", "2")
//        )
//
//        // 미션 필터링 함수
//        fun renderMissions(predicate: (Array<String>) -> Boolean) {
//            missionContainer.removeAllViews()
//
//            val missionCount = mission_content.size
//            for (i in 0 until missionCount) {
//                val mission = mission_content[i]
//
//                if (predicate(mission)) {
//                    val missionView = inflater.inflate(
//                        R.layout.layout_mission_box_mode,
//                        missionContainer,
//                        false
//                    )
//
//                    val title = missionView.findViewById<TextView>(R.id.home_mission_text_mode)
//                    val point = missionView.findViewById<TextView>(R.id.home_mission_text_point_mode)
//                    val people = missionView.findViewById<TextView>(R.id.home_mission_text_people_mode)
//
//                    title.text = mission[3]             // 미션명
//                    point.text = mission[4] + "P"       // 포인트
//                    people.text = mission[5] + "명"      // 몇 명
//
//                    missionContainer.addView(missionView)
//                }
//            }
//        }
//
//        // 함수 이용
//        renderMissions { mission -> mission[2] == "1" }
//        binding.homeButtonTypeMission1.setOnClickListener {
//            renderMissions { mission -> mission[2] == "1" }
//        }
//        binding.homeButtonTypeMission2.setOnClickListener {
//            renderMissions { mission -> mission[1] == "1" }
//        }
//        binding.homeButtonTypeMission3.setOnClickListener {
//            renderMissions { mission -> mission[1] == "0" }
//        }


        // ==============================================================================
        // layout_mission_box_mode 그리는 함수 == renderMissions()
        val missionContainer = binding.homeMissionListContainer
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
                    missionView.findViewById<ImageView>(R.id.home_mission_image_mode)


                // title, point
                title.text = mission.title
                point.text = "${mission.missionPoint}P"

                // people
                val peopleCount = mission.participationCount ?: 0
                people.text = "${peopleCount}명"

                // icon
                Glide.with(missionView)
                    .load(mission.iconImageUrl)
                    .placeholder(R.drawable.ic_mission) // 로딩 중/에러 시 기본 아이콘
                    .error(R.drawable.ic_mission)
                    .into(icon)

                // ==============================================================================
                // 클릭 == 미션 이벤트 박스
                missionView.setOnClickListener {
                    val bundle = Bundle().apply {
                        putInt("missionId", mission.missionId.toInt())
                        putString("title", mission.title)
                        putInt("missionPoint", mission.missionPoint)
                        putString("createdAt", mission.createdAt)
                        putString("iconImageUrl", mission.iconImageUrl)
                    }
                    findNavController().navigate(R.id.move_home_to_camera, bundle)
                }
                missionContainer.addView(missionView)
            }
        }


        // 2. "상시 미션" 버튼 클릭 시
        binding.homeButtonTypeMission2.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val api = ApiClient.getMissionsApi(requireContext())
                    val missions = api.getRegularMissions()
                    Log.d("MissionAPI", "getRegularMissions 성공, missions size = ${missions.size}")

                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val missionsJson = gson.toJson(missions)
                    Log.d("MissionAPI_FULL_RESPONSE", missionsJson)

                    renderMissions(missions)    // 미션박스 생성 함수
                } catch (e: HttpException) {
                    Log.e("MissionAPI", "getRegularMissions 실패, HTTP code = ${e.code()}, message = ${e.message()}", e)
                    Toast.makeText(requireContext(), "미션 목록 로딩 실패: ${e.code()}", Toast.LENGTH_SHORT).show()
                } catch (e: Throwable) {
                    Log.e("MissionAPI", "getRegularMissions 호출 중 기타 오류: ${e.localizedMessage}", e)
                    Toast.makeText(requireContext(), "에러: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }


        // 3. "돌발 미션" 버튼 클릭 시
        binding.homeButtonTypeMission3.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val api = ApiClient.getMissionsApi(requireContext())
                    val missions = api.getEventMissions()
                    Log.d("MissionAPI", "getEventMissions 성공, missions size = ${missions.size}")

                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val missionsJson = gson.toJson(missions)
                    Log.d("MissionAPI_FULL_RESPONSE", missionsJson)

                    renderMissions(missions)    // 미션박스 생성 함수
                } catch (e: HttpException) {
                    Log.e("MissionAPI", "getEventMissions 실패, HTTP code = ${e.code()}, message = ${e.message()}", e)
                    Toast.makeText(requireContext(), "미션 목록 로딩 실패: ${e.code()}", Toast.LENGTH_SHORT).show()
                } catch (e: Throwable) {
                    Log.e("MissionAPI", "getEventMissions 호출 중 기타 오류: ${e.localizedMessage}", e)
                    Toast.makeText(requireContext(), "에러: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class ImagePagerAdapter(
        private val images: List<Int>
    ) : RecyclerView.Adapter<ImagePagerAdapter.VH>() {
        inner class VH(val iv: ImageView) : RecyclerView.ViewHolder(iv)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val imageView = ImageView(parent.context).apply {
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            return VH(imageView)
        }
        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.iv.setImageResource(images[position])
        }
        override fun getItemCount(): Int = images.size
    }
}