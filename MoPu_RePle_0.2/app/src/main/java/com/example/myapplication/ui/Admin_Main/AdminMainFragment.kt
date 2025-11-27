package com.example.myapplication.ui.Admin_Main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R          // ✅ 이거 하나만
import com.example.myapplication.databinding.FragmentAdminMainBinding

class AdminMainFragment : Fragment() {

    private var _binding: FragmentAdminMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminMainBinding.inflate(inflater, container, false)
        val root = binding.root

        // 미션 설정 버튼 → 관리자 미션 생성 화면 이동
        binding.adminMainButton1MissionSetting.setOnClickListener {
            findNavController().navigate(R.id.move_admin_main_to_admin_mission_create)
        }

        // 미션 목록 확인 버튼 → 관리자 미션 리스트 화면 이동
        binding.adminMainButton2MissionListCheck.setOnClickListener {
            findNavController().navigate(R.id.move_admin_main_to_admin_mission_list)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
