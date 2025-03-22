package com.col.eventradar.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.col.eventradar.MainActivity
import com.col.eventradar.NavGraphDirections
import com.col.eventradar.databinding.FragmentLoginBinding
import com.col.eventradar.ui.components.ToastFragment
import com.col.eventradar.ui.viewmodels.LoginViewModel
import com.col.eventradar.ui.viewmodels.LoginViewModelFactory

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var loginViewModel: LoginViewModel
    private val toastFragment = ToastFragment()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        loginViewModel =
            ViewModelProvider(
                this,
                LoginViewModelFactory(requireContext()),
            )[LoginViewModel::class.java]

        loginViewModel.isSignedIn.observe(viewLifecycleOwner) { isSignedIn ->
            if (isSignedIn == true) {
                (requireActivity() as MainActivity).binding.bottomNavigationView.visibility =
                    View.VISIBLE
                findNavController().navigate(NavGraphDirections.actionGlobalNavigationHome())
            } else {
                toastFragment("Authentication failed")
            }
        }

        binding.loginButton.setOnClickListener {
            loginViewModel.signIn()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
