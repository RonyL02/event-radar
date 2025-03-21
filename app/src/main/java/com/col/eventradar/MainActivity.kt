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

        // ✅ Initialize navController BEFORE using it
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
                binding.bottomNavigationView.visibility = View.GONE

                if (savedInstanceState == null) {
                    navController.navigate(NavGraphDirections.actionGlobalNavigationLogin())
                }
            }
        }

        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val destinationId =
                when (menuItem.itemId) {
                    R.id.navigation_home -> R.id.navigation_home
                    R.id.navigation_comments -> R.id.navigation_comments
                    R.id.navigation_settings -> R.id.navigation_settings
                    R.id.navigation_map -> R.id.navigation_map

                    else -> return@setOnItemSelectedListener false
                }

            navController.navigate(
                destinationId,
                null,
                NavOptions
                    .Builder()
                    .setLaunchSingleTop(true)
                    .setPopUpTo(R.id.navigation_home, false) // 👈 Make sure HOME works
                    .build(),
            )

            true
        }
    }
}
