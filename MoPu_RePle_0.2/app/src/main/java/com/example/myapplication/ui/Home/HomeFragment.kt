package com.example.myapplication.ui.Home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.data.ApiClient
import com.example.myapplication.data.Mission_Data
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.databinding.LayoutMissionBoxModeBinding
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch
import retrofit2.HttpException

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // 리사이클러뷰 어댑터
    private lateinit var missionAdapter: MissionAdapter

    // [검색 핵심] API로 받아온 전체 데이터를 저장해둘 리스트
    private var allLoadedMissions = listOf<Mission_Data>()

    // [핵심 추가] 현재 어떤 미션을 보고 있는지 추적 (regular, event, pending)
    private var currentType = "regular"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 1. 이미지 좌우 슬라이드 영역 설정
        val pager = binding.homeImgSlide1
        val sliderImages = listOf(
            R.drawable.horse_picture,
            R.drawable.main_banner1,
            R.drawable.main_banner2
        )
        pager.adapter = ImagePagerAdapter(sliderImages)

        binding.homeImgButtonSeeAll.setOnClickListener {
            findNavController().navigate(R.id.navigation_gallery)
        }

        // 2. 리사이클러뷰(미션 목록) 설정
        setupRecyclerView()


        // 3. [검색 기능 구현] EditText 입력 감지
        binding.homeSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val searchText = s.toString().trim()
                filterMissions(searchText) // 입력된 글자로 필터링 실행
            }
        })


        // 4. [수정됨] "승인 대기 미션" 버튼 클릭
        binding.homeButtonTypeMission1.setOnClickListener {
            binding.homeTextMission.text = "승인 대기 미션 목록"
            loadMissions("pending")
        }


        // 5. "상시 미션" 버튼 클릭 시
        binding.homeButtonTypeMission2.setOnClickListener {
            binding.homeTextMission.text = "상시 미션 \n오늘도 환경을 잘 부탁해"
            loadMissions("regular")
        }


        // 6. "돌발 미션" 버튼 클릭 시
        binding.homeButtonTypeMission3.setOnClickListener {
            binding.homeTextMission.text = "지금만 할 수 있는 \n돌발 미션!"
            loadMissions("event")
        }

        // 초기 화면 로딩 (상시 미션)
        loadMissions("regular")

        return root
    }

    // 리사이클러뷰 초기화 함수
    private fun setupRecyclerView() {
        missionAdapter = MissionAdapter { mission ->
            // [핵심 수정] 아이템 클릭 시 현재 탭 종류에 따라 이동 경로 분기 처리

            if (currentType == "pending") {
                // 1. 승인 대기 미션 -> 상세 화면(Detail)으로 이동
                // 승인 대기 미션은 participationId가 반드시 존재함
                val participationId = mission.participationId ?: 0L

                // mobile_navigation.xml에 추가한 action ID를 사용하여 이동
                val action = HomeFragmentDirections.actionHomeToHistoryDetail(participationId)
                findNavController().navigate(action)

            } else {
                // 2. 상시/돌발 미션 -> 카메라 인증 화면으로 이동 (기존 로직 유지)
                val bundle = Bundle().apply {
                    putInt("missionId", mission.missionId.toInt())
                    putString("title", mission.title)
                    putInt("missionPoint", mission.missionPoint)
                    putString("createdAt", mission.createdAt)
                    putString("iconImageUrl", mission.iconImageUrl)
                }
                findNavController().navigate(R.id.move_home_to_camera, bundle)
            }
        }

        // XML에서 home_mission_list_container가 RecyclerView여야 함
        binding.homeMissionListContainer.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = missionAdapter
        }
    }

    // API 통신 및 데이터 로드 함수 통합
    private fun loadMissions(type: String) {
        currentType = type // [중요] 현재 탭 상태 저장

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = ApiClient.getMissionsApi(requireContext())
                val missions: List<Mission_Data>

                // 타입에 따라 다른 API 함수 호출 및 데이터 처리
                if (type == "pending") {
                    // [승인 대기] API 호출
                    val pendingList = api.getPendingMissions()

                    // PendingMission 데이터를 Mission_Data 형태로 변환 (어댑터 재사용을 위해)
                    missions = pendingList.map { pending ->
                        Mission_Data(
                            missionId = pending.missionId,
                            title = pending.title,
                            missionPoint = pending.missionPoint,
                            category = pending.category,
                            iconImageUrl = pending.iconImageUrl,
                            bannerImageUrl = null, // 상세 화면에서 다시 로드하므로 null 처리
                            participationCount = 0, // 대기 목록에서는 참가 인원 0 또는 숨김 처리
                            createdAt = pending.submittedAt, // 제출일시를 생성일시 위치에 저장
                            participationId = pending.participationId // [중요] 상세 이동을 위한 ID 저장
                        )
                    }
                } else if (type == "regular") {
                    // [상시] API 호출
                    missions = api.getRegularMissions()
                } else {
                    // [돌발] API 호출
                    missions = api.getEventMissions()
                }

                Log.d("MissionAPI", "get${type}Missions 성공, size = ${missions.size}")

                // 로그 확인용
                val gson = GsonBuilder().setPrettyPrinting().create()
                Log.d("MissionAPI_Response", gson.toJson(missions))

                // [중요] 1. 전체 데이터를 변수에 저장 (검색을 위해)
                allLoadedMissions = missions

                // [중요] 2. 화면에 뿌리기
                missionAdapter.submitList(missions)

                // 3. 검색창 초기화
                binding.homeSearch.text.clear()

            } catch (e: HttpException) {
                Log.e("MissionAPI", "Load Fail: ${e.code()}", e)
                Toast.makeText(requireContext(), "로딩 실패: ${e.code()}", Toast.LENGTH_SHORT).show()
            } catch (e: Throwable) {
                Log.e("MissionAPI", "Error: ${e.localizedMessage}", e)
                Toast.makeText(requireContext(), "에러 발생", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // [검색 로직] 저장된 allLoadedMissions에서 검색어가 포함된 것만 골라냄
    private fun filterMissions(query: String) {
        val filteredList = if (query.isEmpty()) {
            allLoadedMissions // 검색어 없으면 전체 보여줌
        } else {
            allLoadedMissions.filter { mission ->
                // 제목(title)에 검색어가 포함되어 있는지 확인 (대소문자 무시)
                mission.title.contains(query, ignoreCase = true)
            }
        }
        // 걸러진 리스트를 어댑터에 전달 -> 화면 갱신
        missionAdapter.submitList(filteredList)
    }

    override fun onResume() {
        super.onResume()
        // 앱바(보라색 타이틀바)를 강제로 숨깁니다.
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.hide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // =====================================================================
    // 내부 클래스 1: 이미지 슬라이더 어댑터
    private class ImagePagerAdapter(private val images: List<Int>) :
        RecyclerView.Adapter<ImagePagerAdapter.VH>() {
        inner class VH(val iv: ImageView) : RecyclerView.ViewHolder(iv)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val imageView = ImageView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
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

    // =====================================================================
    // 내부 클래스 2: 미션 리스트 어댑터 (RecyclerView.Adapter)
    class MissionAdapter(val onClick: (Mission_Data) -> Unit) :
        RecyclerView.Adapter<MissionAdapter.MissionViewHolder>() {

        private val items = mutableListOf<Mission_Data>()

        fun submitList(newItems: List<Mission_Data>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionViewHolder {
            // layout_mission_box_mode.xml을 바인딩으로 연결
            val binding = LayoutMissionBoxModeBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return MissionViewHolder(binding)
        }

        override fun onBindViewHolder(holder: MissionViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        inner class MissionViewHolder(private val binding: LayoutMissionBoxModeBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(item: Mission_Data) {
                binding.homeMissionTextMode.text = item.title
                binding.homeMissionTextPointMode.text = "${item.missionPoint}P"

                val count = item.participationCount ?: 0
                binding.homeMissionTextPeopleMode.text = "${count}명"

                // Glide로 이미지 로드
                Glide.with(itemView.context)
                    .load(item.iconImageUrl)
                    .placeholder(R.drawable.ic_mission)
                    .error(R.drawable.ic_mission)
                    .into(binding.homeMissionIconMode)

                // 클릭 이벤트
                itemView.setOnClickListener {
                    onClick(item)
                }
            }
        }
    }
}