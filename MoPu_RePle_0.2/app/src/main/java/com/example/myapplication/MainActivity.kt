package com.example.myapplication

import android.R.attr.visibility
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // 목적지에 따라 액션바 / 바텀 네비바 보이기/숨기기
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {


                R.id.navigation_login,
                R.id.navigation_signup,
                R.id.navigation_admin_main,
                R.id.navigation_admin_mission_list,
                R.id.navigation_admin_mission_create,
                R.id.navigation_admin_pending_mission_detail,

                // 액션바, 네비바 둘 다 안 보임 == login, signup, admin_main, admin_mission_list
                R.id.navigation_login, R.id.navigation_signup, R.id.navigation_admin_main, R.id.navigation_admin_mission_list, R.id.navigation_gallery -> {
                    supportActionBar?.hide()
                    binding.navView.visibility = View.GONE
                }



                // 액션바만 숨기고 네비바는 보이게
                R.id.navigation_home,
                R.id.navigation_point,
                R.id.navigation_my_page,
                R.id.navigation_mission_camera -> {
                    supportActionBar?.hide()
                    binding.navView.visibility = View.VISIBLE
                }

                // 기본: 액션바 + 네비바 모두 보이게
                else -> {
                    supportActionBar?.show()
                    binding.navView.visibility = View.VISIBLE
                }
            }
        }

        // 상단 액션바-Navigation 연동 (홈/포인트/마이페이지만 top level)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_point,
                R.id.navigation_my_page
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}
