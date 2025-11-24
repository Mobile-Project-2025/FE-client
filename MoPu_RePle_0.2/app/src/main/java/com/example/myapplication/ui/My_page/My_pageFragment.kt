package com.example.myapplication.ui.My_page

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.databinding.FragmentMyPageBinding
import com.example.myapplication.ui.UserViewModel

class My_pageFragment : Fragment() {

    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!

    // [추가] 뷰모델 연결 (PointFragment와 데이터 공유)
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 기존 코드 유지 (로그아웃 버튼 밑줄 효과)
        binding.mpLogout.apply {
            paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }

        // [추가] 2. 서버에서 내 정보 가져오기 실행
        userViewModel.fetchUserInfo()

        // [추가] 3. 닉네임이 바뀌면 화면에 바로 반영
        userViewModel.userNickname.observe(viewLifecycleOwner) { nickname ->
            // fragment_my_page.xml의 user_nickname 아이디에 연결
            binding.userNickname.text = nickname
        }

        // [추가] 4. 포인트가 바뀌면 화면에 바로 반영
        userViewModel.userPoints.observe(viewLifecycleOwner) { points ->
            // fragment_my_page.xml의 user_point 아이디에 연결
            binding.userPoint.text = "${points}p"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}