package com.col.eventradar.ui.bottom_sheets

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.col.eventradar.data.repository.CommentsRepository
import com.col.eventradar.databinding.FragmentEditCommentBottomSheetBinding
import com.col.eventradar.models.common.PopulatedComment
import com.col.eventradar.ui.viewmodels.UserViewModel
import com.col.eventradar.ui.viewmodels.UserViewModelFactory
import com.col.eventradar.utils.ImageUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditCommentBottomSheetFragment(
    private val populatedComment: PopulatedComment,
    private val onCommentUpdated: () -> Unit, // Callback to refresh UI
) : BottomSheetDialogFragment() {
    private var bindingInternal: FragmentEditCommentBottomSheetBinding? = null
    private val binding get() = bindingInternal!!

    private val userViewModel: UserViewModel by activityViewModels {
        val commentRepository = CommentsRepository(requireContext())
        UserViewModelFactory(commentRepository)
    }

    private var selectedImageUri: Uri? = null
    private var initialContent: String? = populatedComment.comment.content
    private var comment = populatedComment.comment

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
        bindingInternal =
            FragmentEditCommentBottomSheetBinding.inflate(inflater, container, false)

        setupUI()
        setupListeners()

        return binding.root
    }

    private fun setupUI() {
        with(binding) {
            editCommentInput.setText(comment.content)
            val imageUrl = comment.imageUrl

            if (!imageUrl.isNullOrBlank()) {
                editCommentImagePreviewCard.visibility = View.VISIBLE
                editCommentImagePreview.visibility = View.VISIBLE
                ImageUtils.showImgInViewFromUrl(
                    imageUrl,
                    editCommentImagePreview,
                    editCommentImageLoader,
                )
            } else {
                editCommentImagePreviewCard.visibility = View.GONE
            }
        }
    }

    private fun setupListeners() {
        with(binding) {
            editAddImageButton.setOnClickListener {
                imagePickerLauncher.launch("image/*")
            }

            saveCommentButton.setOnClickListener {
                val updatedContent = editCommentInput.text.toString().trim()

                if (updatedContent.isNotEmpty() || selectedImageUri != null) {
                    CoroutineScope(Dispatchers.Main).launch {
                        userViewModel.updateComment(
                            commentId = comment.id,
                            updatedContent = updatedContent.takeIf { it != initialContent },
                            newImageUri = selectedImageUri,
                        )

                        dismiss()
                        onCommentUpdated()
                    }
                }
            }
        }
    }

    private fun showImagePreview(imageUri: Uri) {
        with(binding) {
            editCommentImagePreviewCard.visibility = View.VISIBLE
            editCommentImagePreview.visibility = View.VISIBLE
            ImageUtils.showImgInViewFromUrl(
                imageUri.toString(),
                editCommentImagePreview,
                editCommentImageLoader,
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingInternal = null
    }

    companion object {
        const val TAG = "EditCommentBottomSheet"
    }
}
