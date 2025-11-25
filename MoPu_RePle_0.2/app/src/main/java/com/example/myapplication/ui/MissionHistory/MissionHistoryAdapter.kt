package com.example.myapplication.ui.MissionHistory

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R // 본인 패키지명 확인
import com.example.myapplication.data.MissionHistoryItem
import com.example.myapplication.databinding.ItemMissionHistoryBinding

class MissionHistoryAdapter(
    private val onClick: (Long) -> Unit
) : RecyclerView.Adapter<MissionHistoryAdapter.ViewHolder>() {

    private var items = listOf<MissionHistoryItem>()

    fun submitList(list: List<MissionHistoryItem>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMissionHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemMissionHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MissionHistoryItem) {
            binding.tvHistoryTitle.text = item.title
            binding.tvHistoryPoint.text = "${item.missionPoint}p"
            binding.tvHistoryCount.text = "${item.participationCount}"

            // 이미지 로드
            if (!item.iconUrl.isNullOrEmpty()) {
                Glide.with(itemView.context).load(item.iconUrl).into(binding.imgHistoryIcon)
            }

            // 상태에 따른 아이콘 변경
            // (drawable 리소스를 프로젝트에 맞게 변경해주세요. 여기선 안드로이드 기본 아이콘 활용 예시)
            when (item.participationStatus) {
                "APPROVED" -> {
                    binding.imgStatusIcon.setImageResource(R.drawable.ic_check_green)
                    binding.imgStatusIcon.clearColorFilter()
                }
                "PENDING" -> {
                    binding.imgStatusIcon.setImageResource(R.drawable.ic_check_pending)
                    binding.imgStatusIcon.clearColorFilter()
                }
                "REJECTED" -> {
                    binding.imgStatusIcon.setImageResource(R.drawable.ic_check_red)
                    binding.imgStatusIcon.clearColorFilter()
                }
                else -> {
                    binding.imgStatusIcon.setImageDrawable(null)
                }
            }

            itemView.setOnClickListener {
                onClick(item.participationId)
            }
        }
    }
}