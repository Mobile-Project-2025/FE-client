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
import com.google.gson.GsonBuilder
import android.text.Editable
import android.text.TextWatcher

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    // 🔍 검색을 위해 마지막으로 로드된 미션들을 저장해둘 리스트
    private var allLoadedMissions: List<Mission_Data> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 이미지 좌우 슬라이드 영역
        val pager = root.findViewById<ViewPager2>(R.id.home_img_slide_1)

        // 홈 배너 이미지 목록 (갤러리 전체보기와 동일한 리스트)
        val sliderImages = listOf(
            R.drawable.horse_picture,
            R.drawable.main_banner1,
            R.drawable.main_banner2
        )
        pager.adapter = ImagePagerAdapter(sliderImages) {
            findNavController().navigate(R.id.move_home_to_gallery)
        }


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
                    missionView.findViewById<ImageView>(R.id.home_mission_icon_mode)

                // title, point
                title.text = mission.title
                point.text = "${mission.missionPoint}P"

                // people
                val peopleCount = mission.participationCount ?: 0
                people.text = "${peopleCount}명"

                // icon
                Glide.with(missionView)
                    .load(mission.iconImageUrl)
                    .placeholder(R.drawable.ic_mission)
                    .error(R.drawable.ic_mission)
                    .into(icon)


                // ==============================================================================
                // 미션박스에 클릭 이벤트 추가 == 미션 이벤트 박스
                missionView.setOnClickListener {
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            val api = ApiClient.getMissionsApi(requireContext())
                            val detail = api.getMissionDetail(mission.missionId.toInt())
                            Log.d("MissionAPI", "getMissionDetail 성공, missionId=${detail.missionId}")

                            val bundle = Bundle().apply {
                                putInt("missionId", detail.missionId)
                                putString("title", detail.title)
                                putString("content", detail.content)
                                putInt("missionPoint", detail.missionPoint)
                                putString("iconImageUrl", detail.iconImageUrl)
                                putString("startDate", detail.startDate)
                                putString("deadLine", detail.deadLine)
                                // null이면 0으로
                                putInt("participationCount", detail.participationCount ?: 0)
                                putBoolean("hasSubmitted", detail.hasSubmitted)
                            }
                            findNavController().navigate(R.id.move_home_to_camera, bundle)
                        } catch (e: HttpException) {
                            Log.e(
                                "MissionAPI",
                                "getMissionDetail 실패, HTTP code = ${e.code()}, message = ${e.message()}",
                                e
                            )
                            Toast.makeText(
                                requireContext(),
                                "미션 상세 조회 실패: ${e.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Throwable) {
                            Log.e(
                                "MissionAPI",
                                "getMissionDetail 호출 중 기타 오류: ${e.localizedMessage}",
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
                missionContainer.addView(missionView)
            }
        }


        // 검색 로직
        fun filterMissions(query: String) {
            val filtered = if (query.isBlank()) {
                allLoadedMissions    // 검색어 없으면 전체
            } else {
                allLoadedMissions.filter { mission ->
                    mission.title.contains(query, ignoreCase = true)
                }
            }
            renderMissions(filtered)
        }

        // 검색 EditText에 TextWatcher 연결
        binding.homeSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) { }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) { }

            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString().orEmpty()
                filterMissions(query)
            }
        })


        // ========================================================================================
        // 1. "승인 대기 미션" 버튼 클릭 시
        binding.homeButtonTypeMission1.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val api = ApiClient.getMissionsApi(requireContext())
                    val missions = api.getPendingMissions_STUDENT()
                    Log.d("MissionAPI", "getUserPendingMissions 성공, missions size = ${missions.size}")

                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val missionsJson = gson.toJson(missions)
                    Log.d("MissionAPI_FULL_RESPONSE", missionsJson)

                    allLoadedMissions = missions
                    renderMissions(missions)    // 미션박스 생성 함수
                } catch (e: HttpException) {
                    Log.e("MissionAPI", "getUserPendingMissions 실패, HTTP code = ${e.code()}, message = ${e.message()}", e)
                    Toast.makeText(requireContext(), "승인 대기 미션 목록 로딩 실패: ${e.code()}", Toast.LENGTH_SHORT).show()
                } catch (e: Throwable) {
                    Log.e("MissionAPI", "getUserPendingMissions 호출 중 기타 오류: ${e.localizedMessage}", e)
                    Toast.makeText(requireContext(), "에러: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
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

                    allLoadedMissions = missions
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

                    allLoadedMissions = missions
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

    // 뒤로가기 눌러도, 상단 액션바 안 뜨게 방지
    override fun onResume() {
        super.onResume()
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.hide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    // 상단 이미지 스와이프
    private class ImagePagerAdapter(
        private val images: List<Int>,
        private val onImageClick: () -> Unit   // 클릭 시
    ) : RecyclerView.Adapter<ImagePagerAdapter.VH>() {

        inner class VH(val iv: ImageView) : RecyclerView.ViewHolder(iv) {
            init {
                iv.setOnClickListener {
                    onImageClick()
                }
            }
        }

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