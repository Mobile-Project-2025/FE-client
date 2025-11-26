package com.example.myapplication.ui.MissionHistory

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.data.ApiClient
import com.example.myapplication.databinding.FragmentMissionHistoryListBinding
import kotlinx.coroutines.launch

class MissionHistoryFragment : Fragment(R.layout.fragment_mission_history_list) {

    private lateinit var binding: FragmentMissionHistoryListBinding
    private lateinit var adapter: MissionHistoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMissionHistoryListBinding.bind(view)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        adapter = MissionHistoryAdapter { participationId ->
            // 상세 화면으로 이동 (nav_graph에 action 추가 필요)
            val action = MissionHistoryFragmentDirections.actionHistoryListToDetail(participationId)
            findNavController().navigate(action)
        }
        binding.recyclerHistory.adapter = adapter

        fetchHistory()
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }
    private fun fetchHistory() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.getMissionsApi(requireContext())
                val list = api.getMissionHistory()
                adapter.submitList(list)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}