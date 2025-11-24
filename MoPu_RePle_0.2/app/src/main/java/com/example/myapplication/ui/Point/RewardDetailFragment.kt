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

        // 상단바 숨기기
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

        // 1. 내 포인트 상태에 따라 버튼 활성화/비활성화 (기존 유지)
        userViewModel.userPoints.observe(viewLifecycleOwner) { myPoints ->
            if (myPoints >= price) {
                binding.btnPurchase.isEnabled = true
                binding.btnPurchase.text = "구매하기"
                binding.btnPurchase.setBackgroundColor(Color.parseColor("#8BC34A"))
            } else {
                binding.btnPurchase.isEnabled = false
                binding.btnPurchase.text = "포인트 부족"
                binding.btnPurchase.setBackgroundColor(Color.LTGRAY)
            }
        }

        // [수정됨] 2. 구매 성공 여부 관찰 (서버 응답이 오면 실행됨)
        userViewModel.purchaseSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(context, "구매가 완료되었습니다!", Toast.LENGTH_SHORT).show()
                // 성공 시 뒤로 가기
                findNavController().popBackStack()
            }
        }

        // [수정됨] 3. 버튼 클릭 시 서버에 요청 보내기
        binding.btnPurchase.setOnClickListener {
            // 더 이상 결과를 변수(val)로 받지 않고, 뷰모델에 "요청해줘!"라고 명령만 내립니다.
            userViewModel.purchaseItem(price)
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
        _binding = null
    }
}