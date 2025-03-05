package com.col.eventradar

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.col.eventradar.auth.GoogleAuthClient
import com.col.eventradar.databinding.ActivityMainBinding
import com.col.eventradar.ui.components.ToastFragment
import com.col.eventradar.ui.viewmodels.LoginViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var loginViewModel: LoginViewModel
    private val toastFragment = ToastFragment()

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

        loginViewModel =
            ViewModelProvider(this)[LoginViewModel::class.java]
    }

    override fun onStart() {
        super.onStart()

        navController = findNavController(binding.navHostContainer.id)

        loginViewModel.isSignedIn.observe(this) { isSignedIn ->
            if (isSignedIn == null) {
                navController.navigate(NavGraphDirections.actionGlobalNavigationLogin())
                binding.bottomNavigationView.visibility = View.INVISIBLE
            } else if (isSignedIn == true) {
//                navController.navigate(NavGraphDirections.actionGlobalNavigationHome())
                binding.bottomNavigationView.visibility = View.VISIBLE
            } else {
                toastFragment("Authentication failed")
            }
        }


        binding.bottomNavigationView.setupWithNavController(navController)
    }
}
