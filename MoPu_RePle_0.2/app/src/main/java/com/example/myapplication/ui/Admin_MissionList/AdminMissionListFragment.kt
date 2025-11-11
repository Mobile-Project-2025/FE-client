package com.example.myapplication.ui.Admin_MissionList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentAdminMissionListBinding

class AdminMissionListFragment : Fragment() {

    private var _binding: FragmentAdminMissionListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminMissionListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 미션 관련 부분
        val missionContainer = binding.adminMissionListContainer
        // 미션 데이터 2차원 배열
        val mission_content = arrayOf(
            //
            // 6개 정보 배열
            // ID | 고정 미션 여부 | 승인 대기 여부 | 이름 | 포인트 | 인원
            //
            // 승인대기 여부의 경우
            // 0 == 생성은 되었으나, 참여는 아직 안함
            // 1 == 참여는 했고, 승인 대기 중
            // 2 == 기간이 지나서 마감됨
            // 3 == 관리자가 종료 처리함
            arrayOf("1", "1", "0", "페트병 버리기", "300", "10"),
            arrayOf("2", "1", "0", "분리수거", "200", "5"),
            arrayOf("3", "0", "1", "이거 승인 대기 중임", "67", "15"),
            arrayOf("4", "0", "2", "이거 기간 지나서 마감", "49", "25"),
            arrayOf("5", "0", "3", "이거 종료 처리한 거임", "999", "2")
        )

        // 미션 필터링 함수
        fun renderMissions(predicate: (Array<String>) -> Boolean) {
            missionContainer.removeAllViews()

            val missionCount = mission_content.size
            for (i in 0 until missionCount) {
                val mission = mission_content[i]

                if (predicate(mission)) {
                    val missionView = inflater.inflate(
                        R.layout.layout_mission_box_mode,
                        missionContainer,
                        false
                    )

                    val title = missionView.findViewById<TextView>(R.id.home_mission_text_mode)
                    val point = missionView.findViewById<TextView>(R.id.home_mission_text_point_mode)
                    val people = missionView.findViewById<TextView>(R.id.home_mission_text_people_mode)

                    title.text = mission[3]             // 미션명
                    point.text = mission[4] + "P"       // 포인트
                    people.text = mission[5] + "명"      // 몇 명

                    missionContainer.addView(missionView)
                }
            }
        }

        // 함수 이용
        renderMissions { mission -> mission[2] == "1" }
        binding.adminMissionButtonTypeMission1.setOnClickListener {
            renderMissions { mission -> mission[2] == "1" }
        }
        binding.adminMissionButtonTypeMission2.setOnClickListener {
            renderMissions { mission -> mission[2] == "2" }
        }
        binding.adminMissionButtonTypeMission3.setOnClickListener {
            renderMissions { mission -> mission[2] == "3" }
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