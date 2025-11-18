package com.example.myapplication.ui.Point

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.navigation.fragment.findNavController
import com.example.myapplication.databinding.LayoutMissionItemDetailBinding
import java.text.NumberFormat
import java.util.Locale

class RewardDetailFragment : Fragment() {

    private var _binding: LayoutMissionItemDetailBinding? = null
    private val binding get() = _binding!!

    private val args: RewardDetailFragmentArgs by navArgs()

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

        // [핵심 2] 우리가 만든 XML 속의 뒤로가기 버튼(btnBack)에 기능을 연결합니다.
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