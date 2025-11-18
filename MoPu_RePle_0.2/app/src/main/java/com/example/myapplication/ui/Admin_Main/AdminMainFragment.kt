package com.example.myapplication.ui.Admin_Main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
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
        val root: View = binding.root

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