package com.example.myapplication.ui.Home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.R
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.view.ViewGroup.LayoutParams
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
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


        // 미션 관련 부분
        val missionContainer = binding.homeLinear0

        // 미션 데이터 2차원 배열
        val mission_content = arrayOf(

            // 6개 정보 배열
            // ID | 고정 미션 여부 | 승인 대기 여부 | 이름 | 포인트 | 인원
            arrayOf("1", "1", "0", "페트병 버리기", "300", "10"),
            arrayOf("2", "1", "0", "분리수거", "200", "5")

        )
        // 미션 개수
        val mission_count = mission_content.size
        for (i in 0 until mission_count) {
            val missionView = inflater.inflate(R.layout.layout_mission_box_mode, missionContainer, false)

            val title = missionView.findViewById<TextView>(R.id.home_mission_text_mode)
            val point = missionView.findViewById<TextView>(R.id.home_mission_text_point_mode)
            val people = missionView.findViewById<TextView>(R.id.home_mission_text_people_mode)

            title.text = mission_content[i][3]          // 미션명
            point.text = mission_content[i][4] + "P"    // 포인트
            people.text = mission_content[i][5] + "명"   // 몇 명

            missionContainer.addView(missionView)
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