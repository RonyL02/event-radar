package com.col.eventradar.ui.bottom_sheets

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.col.eventradar.databinding.FragmentEditProfileBottomSheetBinding
import com.col.eventradar.ui.viewmodels.UserViewModel
import com.col.eventradar.utils.ImageUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditProfileBottomSheetFragment(
    private val onProfileUpdated: () -> Unit,
) : BottomSheetDialogFragment() {
    private var bindingInternal: FragmentEditProfileBottomSheetBinding? = null
    private val binding get() = bindingInternal!!

    private val userViewModel: UserViewModel by activityViewModels()

    private var selectedImageUri: Uri? = null
    private var initialUsername: String? = null

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                showImagePreview(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bindingInternal = FragmentEditProfileBottomSheetBinding.inflate(inflater, container, false)

        setupUI()
        setupListeners()

        return binding.root
    }

    private fun setupUI() {
        userViewModel.loggedInUser.value?.let { user ->
            initialUsername = user.username
            with(binding) {
                editUsernameInput.setText(user.username)
                user.imageUri?.let { imageUrl ->
                    editProfileImagePreviewCard.visibility = View.VISIBLE
                    editProfileImagePreview.visibility = View.VISIBLE
                    ImageUtils.showImgInViewFromUrl(
                        imageUrl,
                        editProfileImagePreview,
                        editProfileImageLoader,
                    )
                }
            }
        }
    }

    private fun setupListeners() {
        with(binding) {
            editAddProfileImageButton.setOnClickListener {
                imagePickerLauncher.launch("image/*")
            }

            updateProfileButton.setOnClickListener {
                val updatedUsername = editUsernameInput.text.toString().trim()

                if (updatedUsername.isNotEmpty() || selectedImageUri != null) {
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            userViewModel.updateUserProfile(
                                updatedUsername.takeIf { it != initialUsername },
                                selectedImageUri,
                            )

                            userViewModel.loggedInUser.observe(viewLifecycleOwner) { updatedUser ->
                                if (updatedUser?.imageUri != null || updatedUser?.username != initialUsername) {
                                    dismiss()
                                    onProfileUpdated()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("EditProfile", "Error updating profile", e)
                        }
                    }
                }
            }
        }
    }

    private fun showImagePreview(imageUri: Uri) {
        with(binding) {
            editProfileImagePreviewCard.visibility = View.VISIBLE
            editProfileImagePreview.visibility = View.VISIBLE
            ImageUtils.showImgInViewFromUrl(
                imageUri.toString(),
                editProfileImagePreview,
                editProfileImageLoader,
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingInternal = null
    }

    companion object {
        const val TAG = "EditProfileBottomSheet"
    }
}
