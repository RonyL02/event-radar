package com.col.eventradar.ui.views

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.col.eventradar.data.remote.UserRepository
import com.col.eventradar.data.repository.CommentsRepository
import com.col.eventradar.databinding.FragmentSettingsBinding
import com.col.eventradar.ui.bottom_sheets.EditProfileBottomSheetFragment
import com.col.eventradar.ui.viewmodels.UserViewModel
import com.col.eventradar.ui.viewmodels.UserViewModelFactory
import com.col.eventradar.utils.ImageUtils

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    private val userViewModel: UserViewModel by activityViewModels {
        val commentRepository = CommentsRepository(requireContext())
        val userRepository = UserRepository(requireContext())
        UserViewModelFactory(commentRepository, userRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        observeViewModel()
        return binding.root
    }

    private fun observeViewModel() =
        userViewModel.loggedInUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                with(binding) {
                    username.text = it.username

                    it.imageUri?.let { imageUri ->
                        profileImage.visibility = View.VISIBLE
                        ImageUtils.showImgInViewFromUrl(imageUri, profileImage, profileImageLoader)
                    }
                }
            }
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupThemeSpinner()
        setupUI()
    }

    private fun setupUI() =
        with(binding) {
            editButton.setOnClickListener {
                val editProfileModal =
                    EditProfileBottomSheetFragment {
                        Toast
                            .makeText(binding.root.context, "Profile Updated", Toast.LENGTH_SHORT)
                            .show()
                    }
                editProfileModal.show(parentFragmentManager, EditProfileBottomSheetFragment.TAG)
            }
        }

    private fun setupThemeSpinner() =
        with(binding) {
            val themes = arrayOf("Light", "Dark")
            themeSpinner.adapter =
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    themes,
                )

            val savedTheme = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO)
            themeSpinner.setSelection(if (savedTheme == AppCompatDelegate.MODE_NIGHT_YES) 1 else 0)

            themeSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long,
                    ) {
                        val selectedTheme =
                            if (position == 1) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "SettingsFragment"
    }
}
