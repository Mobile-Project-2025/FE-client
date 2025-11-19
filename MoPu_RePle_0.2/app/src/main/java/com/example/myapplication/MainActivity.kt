package com.example.myapplication

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



        // 상단 액션바 , 하단 네비바 관련
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {

                // 액션바, 네비바 둘 다 안 보임
                R.id.navigation_login,
                R.id.navigation_signup,
                R.id.navigation_admin_main,
                R.id.navigation_admin_mission_list,
                R.id.navigation_admin_mission_create -> {   // ← 여기에 추가!
                    supportActionBar?.hide()
                    binding.navView.visibility = View.GONE
                }

                // 액션바만 숨기고 네비바는 보임
                R.id.navigation_home,
                R.id.navigation_point,
                R.id.navigation_my_page,
                R.id.navigation_mission_camera -> {
                    supportActionBar?.hide()
                    binding.navView.visibility = View.VISIBLE
                }

                // 기본: 둘 다 보임
                else -> {
                    supportActionBar?.show()
                    binding.navView.visibility = View.VISIBLE
                }
            }

        }



        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_point, R.id.navigation_my_page
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}