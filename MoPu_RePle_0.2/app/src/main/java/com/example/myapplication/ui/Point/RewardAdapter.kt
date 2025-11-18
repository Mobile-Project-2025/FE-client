package com.example.myapplication.ui.Point

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemRewardBinding
import java.text.NumberFormat
import java.util.Locale

class RewardAdapter(val onClick: (RewardItem) -> Unit) : RecyclerView.Adapter<RewardAdapter.VH>() {

    private val items = mutableListOf<RewardItem>()
    private val nf = NumberFormat.getInstance(Locale.KOREA)

    fun submit(list: List<RewardItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemRewardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size

    inner class VH(private val b: ItemRewardBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: RewardItem) {
            b.imgThumb.setImageResource(item.imageRes)
            b.tvBrand.text = item.brand
            b.tvTitle.text = item.title
            b.tvPrice.text = nf.format(item.price)

            itemView.setOnClickListener {
                onClick(item)
            }
        }
    }
}
