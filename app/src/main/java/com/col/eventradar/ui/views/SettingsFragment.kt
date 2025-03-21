package com.col.eventradar.ui.views

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.col.eventradar.NavGraphDirections
import com.col.eventradar.data.remote.UserRepository
import com.col.eventradar.data.repository.CommentsRepository
import com.col.eventradar.databinding.FragmentSettingsBinding
import com.col.eventradar.ui.bottom_sheets.AreasOfInterestBottomSheet
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
        userViewModel.checkUserStatus()
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
        setupUI()
    }

    private fun setupUI() =
        with(binding) {
            logoutButton.setOnClickListener {
                userViewModel.logout()
                Toast
                    .makeText(binding.root.context, "Logged Out Successfully", Toast.LENGTH_SHORT)
                    .show()
                findNavController().navigate(NavGraphDirections.actionGlobalNavigationLogin())
            }
            editButton.setOnClickListener {
                val editProfileModal =
                    EditProfileBottomSheetFragment {
                        Toast
                            .makeText(binding.root.context, "Profile Updated", Toast.LENGTH_SHORT)
                            .show()
                    }
                editProfileModal.show(parentFragmentManager, EditProfileBottomSheetFragment.TAG)
            }
            editAreas.setOnClickListener {
                val areasModal = AreasOfInterestBottomSheet()
                areasModal.show(parentFragmentManager, AreasOfInterestBottomSheet.TAG)
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "SettingsFragment"
    }
}
