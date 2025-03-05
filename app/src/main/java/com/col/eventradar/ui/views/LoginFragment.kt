package com.col.eventradar.ui.views

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.col.eventradar.auth.GoogleAuthClient
import com.col.eventradar.databinding.FragmentLoginBinding
import com.col.eventradar.ui.viewmodels.LoginViewModel

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var loginViewModel: LoginViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        loginViewModel.isSignedIn.observe(viewLifecycleOwner) { isSignedIn ->
            if (isSignedIn == true) {
                val action = LoginFragmentDirections.actionNavGraphToNavigationHome()
                findNavController().navigate(action)
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