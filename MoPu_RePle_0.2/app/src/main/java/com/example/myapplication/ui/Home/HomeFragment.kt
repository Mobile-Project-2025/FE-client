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

    // 검색을 위해 전체 데이터를 저장해둘 리스트
    private var allLoadedMissions = listOf<Mission_Data>()

    // 현재 탭 상태 (regular, event, pending)
    private var currentType = "regular"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 1. 이미지 슬라이더 설정
        val pager = binding.homeImgSlide1
        val sliderImages = listOf(
            R.drawable.main_banner1,
            R.drawable.main_banner2
        )
        // 갤러리로 이동하는 클릭 이벤트 추가
        pager.adapter = ImagePagerAdapter(sliderImages) {
            findNavController().navigate(R.id.move_home_to_gallery)
        }

        binding.homeImgButtonSeeAll.setOnClickListener {
            findNavController().navigate(R.id.move_home_to_gallery) // 네비게이션 ID 확인 필요 (navigation_gallery 인지 move_home_to_gallery 인지)
        }

        // 2. 리사이클러뷰 설정 (핵심 로직)
        setupRecyclerView()

        // 3. 검색 기능 연결
        binding.homeSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val searchText = s.toString().trim()
                filterMissions(searchText)
            }
        })

        // 4. 탭 버튼 클릭 리스너
        // [승인 대기]
        binding.homeButtonTypeMission1.setOnClickListener {
            binding.homeTextMission.text = "승인 대기 미션 목록"
            loadMissions("pending")
        }

        // [상시 미션]
        binding.homeButtonTypeMission2.setOnClickListener {
            binding.homeTextMission.text = "상시 미션 \n오늘도 환경을 잘 부탁해"
            loadMissions("regular")
        }

        // [돌발 미션]
        binding.homeButtonTypeMission3.setOnClickListener {
            binding.homeTextMission.text = "지금만 할 수 있는 \n돌발 미션!"
            loadMissions("event")
        }

        // 초기 화면 로딩
        loadMissions("pending")

        return root
    }

    // 리사이클러뷰 및 클릭 이벤트 설정
    private fun setupRecyclerView() {
        missionAdapter = MissionAdapter { mission ->
            if (currentType == "pending") {
                // 1. 승인 대기 목록인 경우 -> 내역 상세 화면으로 이동
                val participationId = mission.participationId ?: 0L
                val action = HomeFragmentDirections.actionHomeToHistoryDetail(participationId)
                findNavController().navigate(action)
            } else {
                // 2. 상시/돌발 미션인 경우 -> [API 호출] 상세 정보 가져오기 -> 카메라 화면 이동
                fetchMissionDetailAndNavigate(mission.missionId)
            }
        }

        binding.homeMissionListContainer.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = missionAdapter
        }
    }

    // [로컬 파일에서 가져온 로직] 상세 정보 API 호출 후 이동
    private fun fetchMissionDetailAndNavigate(missionId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = ApiClient.getMissionsApi(requireContext())
                // Int로 변환 (API 명세에 따라 다름, 보통 Long 권장이나 기존 코드 따름)
                val detail = api.getMissionDetail(missionId.toInt())
                Log.d("MissionAPI", "getMissionDetail 성공, missionId=${detail.missionId}")

                val bundle = Bundle().apply {
                    putInt("missionId", detail.missionId) // detail 객체 필드명 확인 (Int/Long)
                    putString("title", detail.title)
                    putString("content", detail.content)
                    putInt("missionPoint", detail.missionPoint)
                    putString("iconImageUrl", detail.iconImageUrl)
                    putString("startDate", detail.startDate)
                    putString("deadLine", detail.deadLine)
                    putInt("participationCount", detail.participationCount ?: 0)
                    putBoolean("hasSubmitted", detail.hasSubmitted)
                }
                findNavController().navigate(R.id.move_home_to_camera, bundle)

            } catch (e: HttpException) {
                Log.e("MissionAPI", "상세 조회 실패: ${e.code()}", e)
                Toast.makeText(requireContext(), "상세 정보 로딩 실패", Toast.LENGTH_SHORT).show()
            } catch (e: Throwable) {
                Log.e("MissionAPI", "에러: ${e.message}", e)
                Toast.makeText(requireContext(), "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 서버에서 미션 목록 가져오기
    private fun loadMissions(type: String) {
        currentType = type
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = ApiClient.getMissionsApi(requireContext())
                val missions: List<Mission_Data> = when (type) {
                    "pending" -> {
                        // PendingMission -> Mission_Data 변환
                        api.getPendingMissions().map { pending ->
                            Mission_Data(
                                missionId = pending.missionId,
                                title = pending.title,
                                missionPoint = pending.missionPoint,
                                category = pending.category,
                                iconImageUrl = pending.iconImageUrl,
                                bannerImageUrl = null,
                                participationCount = 0,
                                createdAt = pending.submittedAt,
                                participationId = pending.participationId
                            )
                        }
                    }
                    "regular" -> api.getRegularMissions()
                    "event" -> api.getEventMissions()
                    else -> emptyList()
                }

                Log.d("MissionAPI", "Load $type success, size=${missions.size}")

                allLoadedMissions = missions
                missionAdapter.submitList(missions)
                missionAdapter.submitList(missions)
                binding.homeSearch.text.clear() // 탭 바꿀 때 검색 초기화

            } catch (e: Exception) {
                Log.e("MissionAPI", "Load Error", e)
                Toast.makeText(requireContext(), "데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 검색 필터링
    private fun filterMissions(query: String) {
        val filtered = if (query.isBlank()) allLoadedMissions else {
            allLoadedMissions.filter { it.title.contains(query, ignoreCase = true) }
        }
        missionAdapter.submitList(filtered)
    }

    override fun onResume() {
        super.onResume()
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.hide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // -------------------------------------------------------------
    // 어댑터 클래스들
    // -------------------------------------------------------------

    private class ImagePagerAdapter(
        private val images: List<Int>,
        private val onImageClick: () -> Unit // 클릭 콜백 추가
    ) : RecyclerView.Adapter<ImagePagerAdapter.VH>() {
        inner class VH(val iv: ImageView) : RecyclerView.ViewHolder(iv) {
            init {
                iv.setOnClickListener { onImageClick() }
            }
        }

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

    class MissionAdapter(val onClick: (Mission_Data) -> Unit) :
        RecyclerView.Adapter<MissionAdapter.MissionViewHolder>() {

        private val items = mutableListOf<Mission_Data>()

        fun submitList(newItems: List<Mission_Data>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionViewHolder {
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

                Glide.with(itemView.context)
                    .load(item.iconImageUrl)
                    .placeholder(R.drawable.ic_mission)
                    .error(R.drawable.ic_mission)
                    .into(binding.homeMissionIconMode)

                itemView.setOnClickListener { onClick(item) }
            }
        }
    }
}