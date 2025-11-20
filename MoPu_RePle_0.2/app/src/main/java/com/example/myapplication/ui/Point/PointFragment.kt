package com.example.myapplication.ui.Point

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentPointBinding
import com.example.myapplication.ui.UserViewModel

class PointFragment : Fragment() {

    private var _binding: FragmentPointBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var adapter: RewardAdapter

    private val categories = listOf("전체", "편의점", "뷰티", "카페", "치킨/피자")

    private val allItems = listOf(
        RewardItem("스타벅스", "카페아메리카노 Tall", 5740, "카페", R.drawable.img_starbucks_americano),
        RewardItem("스타벅스", "아이스 카페라떼 Tall", 6440, "카페", R.drawable.img_starbucks_latte),
        RewardItem("에뛰드하우스", "에뛰드 5천원권", 7000, "뷰티", R.drawable.img_etude),
        RewardItem("gs25", "gs25 5000원 교환권", 5000, "편의점", R.drawable.img_gs25),
        RewardItem("BHC", "순살뿌링클+콜라1.25L", 29400, "치킨/피자", R.drawable.img_bhc_bburinkle)
    )


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPointBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvNickname.text = "홍길동님"
        userViewModel.userPoints.observe(viewLifecycleOwner) { points ->
            binding.tvPoints.text = "${points}P"
        }

        binding.chipGroup.removeAllViews()
        categories.forEachIndexed { index, name ->
            val chip = layoutInflater.inflate(R.layout.view_chip_filter, binding.chipGroup, false) as Chip
            chip.text = name
            chip.isChecked = index == 0
            binding.chipGroup.addView(chip)
        }

        adapter = RewardAdapter { item ->
            val action = PointFragmentDirections.actionPointFragmentToRewardDetailFragment(
                brand = item.brand,
                title = item.title,
                price = item.price,
                imageRes = item.imageRes
            )
            findNavController().navigate(action)
        }
        binding.recyclerRewards.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRewards.adapter = adapter

        applyFilter(getSelectedCategory())
        binding.chipGroup.setOnCheckedStateChangeListener { _, _ ->
            applyFilter(getSelectedCategory())
        }
    }

    private fun getSelectedCategory(): String {
        val checked = binding.chipGroup.children.filterIsInstance<Chip>().firstOrNull { it.isChecked }
        return checked?.text?.toString() ?: "전체"
    }

    private fun applyFilter(category: String) {
        val list = if (category == "전체") allItems else allItems.filter { it.category == category }
        adapter.submit(list)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
