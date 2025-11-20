package com.example.myapplication.ui.Home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentGalleryBinding

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    // HomeFragment와 동일한 이미지 리스트 (필요 시 arguments로 받아오도록 수정 가능)
    private val galleryImages = listOf(
        R.drawable.horse_picture,
        R.drawable.main_banner1, // 실제 존재하는 이미지 리소스로 변경 필요할 수 있음
        R.drawable.main_banner2
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 상단바 숨기기 (선택 사항)
        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        // 1. 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 2. 리사이클러뷰 설정 (Grid 2칸)
        binding.recyclerGallery.layoutManager = GridLayoutManager(context, 2) // 2열로 보기
        binding.recyclerGallery.adapter = GalleryAdapter(galleryImages)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
        _binding = null
    }

    // 내부 어댑터 클래스
    inner class GalleryAdapter(private val images: List<Int>) :
        RecyclerView.Adapter<GalleryAdapter.VH>() {

        inner class VH(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            // 이미지를 담을 ImageView를 코드로 생성 (XML 별도 생성 없이 간편하게)
            val imageView = ImageView(parent.context).apply {
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    500 // 이미지 높이 (적절히 조절)
                ).apply {
                    setMargins(16, 16, 16, 16) // 이미지 간 간격
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                // 둥근 모서리 등을 원하면 CardView로 감싸거나 background 설정 필요
            }
            return VH(imageView)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.imageView.setImageResource(images[position])
        }

        override fun getItemCount(): Int = images.size
    }
}