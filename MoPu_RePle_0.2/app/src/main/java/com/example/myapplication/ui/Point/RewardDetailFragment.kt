package com.example.myapplication.ui.Point

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.LayoutMissionItemDetailBinding
import com.example.myapplication.ui.UserViewModel
import java.text.NumberFormat
import java.util.Locale
import android.graphics.Color
import android.widget.Toast
class RewardDetailFragment : Fragment() {

    private var _binding: LayoutMissionItemDetailBinding? = null
    private val binding get() = _binding!!

    private val args: RewardDetailFragmentArgs by navArgs()
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutMissionItemDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // [핵심 1] 이 화면에 들어오면 '기본 상단바(Action Bar)'를 숨깁니다.
        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        val brand = args.brand
        val title = args.title
        val price = args.price
        val imageRes = args.imageRes

        val formattedPrice = NumberFormat.getInstance(Locale.KOREA).format(price)

        binding.imgReward.setImageResource(imageRes)
        binding.tvRewardBrand.text = brand
        binding.tvRewardTitle.text = title
        binding.tvRewardPrice.text = formattedPrice + " 캐시"

        userViewModel.userPoints.observe(viewLifecycleOwner) { myPoints ->
            if (myPoints >= price) {
                binding.btnPurchase.isEnabled = true
                binding.btnPurchase.text = "구매하기"
                binding.btnPurchase.setBackgroundColor(Color.parseColor("#8BC34A")) // 활성 색상 (초록)
            } else {
                binding.btnPurchase.isEnabled = false
                binding.btnPurchase.text = "포인트 부족"
                binding.btnPurchase.setBackgroundColor(Color.LTGRAY) // 비활성 색상 (회색)
            }
        }

        binding.btnPurchase.setOnClickListener {
            val isSuccess = userViewModel.deductPoints(price)

            if (isSuccess) {
                Toast.makeText(context, "구매가 완료되었습니다!", Toast.LENGTH_SHORT).show()

                findNavController().popBackStack()
            } else {
                Toast.makeText(context, "잔액이 부족합니다.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // [핵심 3] 이 화면을 나갈 때는 다시 '기본 상단바'를 보여줍니다. (다른 화면을 위해)
        (activity as? AppCompatActivity)?.supportActionBar?.show()
        _binding = null
    }
}