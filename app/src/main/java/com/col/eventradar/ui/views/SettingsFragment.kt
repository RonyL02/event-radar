package com.col.eventradar.ui.views

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.col.eventradar.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                binding.profileImage.setImageURI(it)
                saveImageUri(it.toString())
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupThemeSpinner()
        setupUI()
    }
    private fun setupUI() {
        binding.logoutButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed() // חזרה למסך הקודם
        }



        val savedImageUri = sharedPreferences.getString("profile_image", null)
        savedImageUri?.let {
            binding.profileImage.setImageURI(Uri.parse(it))
        }


        binding.cameraIcon.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }


        val savedUsername = sharedPreferences.getString("username", "Shiran")
        binding.username.text = savedUsername
        binding.editUsernameField.setText(savedUsername)


        binding.editUsernameField.visibility = View.GONE
        binding.saveUsernameButton.visibility = View.GONE


        binding.editUsername.setOnClickListener {
            binding.username.visibility = View.GONE
            binding.editUsernameField.visibility = View.VISIBLE
            binding.saveUsernameButton.visibility = View.VISIBLE
        }

        binding.saveUsernameButton.setOnClickListener {
            val newName = binding.editUsernameField.text.toString().trim()
            if (newName.isNotEmpty()) {
                binding.username.text = newName
                sharedPreferences.edit().putString("username", newName).apply()
            }
            binding.username.visibility = View.VISIBLE
            binding.editUsernameField.visibility = View.GONE
            binding.saveUsernameButton.visibility = View.GONE
        }
    }

    private fun setupThemeSpinner() {
        val themes = arrayOf("Light", "Dark")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, themes)
        binding.themeSpinner.adapter = adapter


        val savedTheme = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO)
        binding.themeSpinner.setSelection(if (savedTheme == AppCompatDelegate.MODE_NIGHT_YES) 1 else 0)

        binding.themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedTheme = if (position == 1) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                saveTheme(selectedTheme)
                applyTheme(selectedTheme)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun saveTheme(mode: Int) {
        sharedPreferences.edit().putInt("theme_mode", mode).apply()
    }

    private fun applyTheme(mode: Int) {
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun saveImageUri(uri: String) {
        sharedPreferences.edit().putString("profile_image", uri).apply()
    }


}