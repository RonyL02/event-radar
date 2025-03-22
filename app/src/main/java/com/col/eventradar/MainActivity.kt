package com.col.eventradar

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.col.eventradar.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        navController = supportFragmentManager
            .findFragmentById(binding.navHostContainer.id)
            ?.findNavController() ?: throw IllegalStateException("NavHostFragment not found")

        binding.bottomNavigationView.setupWithNavController(navController)
        binding.progressBar.visibility = View.VISIBLE

        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            binding.progressBar.visibility = View.GONE

            if (auth.currentUser != null) {
                binding.bottomNavigationView.visibility = View.VISIBLE

                if (savedInstanceState == null) {
                    navController.navigate(NavGraphDirections.actionGlobalNavigationHome())
                }
            } else {
                hideBottomNav()

                if (savedInstanceState == null) {
                    navController.navigate(NavGraphDirections.actionGlobalNavigationLogin())
                }
            }
        }

        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val direction =
                when (menuItem.itemId) {
                    R.id.navigation_home -> NavGraphDirections.actionGlobalNavigationHome()
                    R.id.navigation_comments -> NavGraphDirections.actionGlobalNavigationComments()
                    R.id.navigation_settings -> NavGraphDirections.actionGlobalNavigationSettings()
                    R.id.navigation_map -> NavGraphDirections.actionGlobalNavigationMap()
                    else -> return@setOnItemSelectedListener false
                }

            navController.navigate(
                direction,
                NavOptions
                    .Builder()
                    .setLaunchSingleTop(true)
                    .setPopUpTo(R.id.navigation_home, false)
                    .build(),
            )

            true
        }
    }

    fun hideBottomNav() {
        binding.bottomNavigationView.visibility = View.GONE
    }
}
