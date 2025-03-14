package com.col.eventradar

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
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
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onStart() {
        super.onStart()

        navController = findNavController(binding.navHostContainer.id)
        binding.progressBar.visibility = View.VISIBLE
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                binding.bottomNavigationView.visibility = View.VISIBLE
                navController.navigate(NavGraphDirections.actionGlobalNavigationHome())
            } else {
                navController.navigate(NavGraphDirections.actionGlobalNavigationLogin())
                binding.bottomNavigationView.visibility = View.GONE
            }
            binding.progressBar.visibility = View.GONE
        }

        binding.bottomNavigationView.setupWithNavController(navController)
    }
}
