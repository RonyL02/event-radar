package com.col.eventradar

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostController: NavHostFragment? =
            supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment?
        navController = navHostController?.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        navController?.let {
            Navigation.setViewNavController(
                bottomNavigationView,
                it,
            )
        }

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    navController?.navigate(R.id.eventCardFragmentList)
                    true
                }
                R.id.navigation_map -> {
                    navController?.navigate(R.id.mapFragment)
                    true
                }
                R.id.navigation_comments -> {
                    //TODO: navController?.navigate(R.id.commentsFragment)
                    true
                }
                R.id.navigation_settings -> {
                    //TODO: navController?.navigate(R.id.settingsFragment)
                    true
                }
                else -> false
            }
        }
    }
}
