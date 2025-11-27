package com.example.myapplication.ui.Home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.LayoutMissionBoxModeBinding // layout_mission_box_mode.xml 바인딩

class MissionAdapter(val onClick: (MissionItem) -> Unit) : RecyclerView.Adapter<MissionAdapter.VH>() {

    private val items = mutableListOf<MissionItem>()

    // 데이터를 갱신하는 함수
    fun submitList(list: List<MissionItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = LayoutMissionBoxModeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size

    inner class VH(private val binding: LayoutMissionBoxModeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MissionItem) {
            binding.homeMissionTextMode.text = item.title
            binding.homeMissionTextPointMode.text = "${item.point}P"
            binding.homeMissionTextPeopleMode.text = "${item.people}명"

            // 아이템 클릭 시 실행할 동작
            itemView.setOnClickListener { onClick(item) }
        }
    }
}