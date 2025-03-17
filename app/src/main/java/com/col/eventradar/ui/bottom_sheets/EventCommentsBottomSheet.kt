package com.col.eventradar.ui.bottom_sheets

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.col.eventradar.data.repository.CommentsRepository
import com.col.eventradar.data.repository.EventRepository
import com.col.eventradar.databinding.FragmentEventCommentsBottomSheetBinding
import com.col.eventradar.ui.adapters.EventCommentRecyclerViewAdapter
import com.col.eventradar.ui.viewmodels.EventViewModel
import com.col.eventradar.ui.viewmodels.EventViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class EventCommentsBottomSheet(
    private val eventId: String,
) : BottomSheetDialogFragment() {
    private var bindingInternal: FragmentEventCommentsBottomSheetBinding? = null
    private val binding get() = bindingInternal!!

    private lateinit var commentsRepository: CommentsRepository
    private lateinit var commentRecyclerAdapter: EventCommentRecyclerViewAdapter

    private var selectedImageUri: Uri? = null

    // ✅ Register an image picker
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                Log.d(TAG, "Image Selected: $uri") // ✅ Debugging Log
                selectedImageUri = uri
                showImagePreview(uri) // ✅ Display the selected image
            } else {
                Log.e(TAG, "No image selected") // ❌ If null, log it
            }
        }

    private val eventViewModel: EventViewModel by activityViewModels {
        val eventRepository = EventRepository(requireContext())
        val commentRepository = CommentsRepository(requireContext())
        EventViewModelFactory(eventRepository, commentRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bindingInternal =
            FragmentEventCommentsBottomSheetBinding.inflate(inflater, container, false)

        commentsRepository = CommentsRepository(requireContext())

        setupRecyclerView()
        observeViewModel()
        setupSendCommentButton()

        binding.previewImageButton.visibility = View.GONE
        binding.previewImageButtonCard.visibility = View.GONE // Hide Preview

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        eventViewModel.fetchComments(eventId) // ✅ Fetch comments when the bottom sheet is opened
    }

    private fun setupRecyclerView() {
        binding.commentsList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            commentRecyclerAdapter =
                EventCommentRecyclerViewAdapter(mutableListOf()) // Start with empty list
            adapter = commentRecyclerAdapter
        }
    }

    private fun observeViewModel() {
        eventViewModel.comments.observe(viewLifecycleOwner) { comments ->
            commentRecyclerAdapter.updateComments(comments) // ✅ Update RecyclerView when comments change
        }
    }

    /**
     * ✅ **Handle adding comments & Image Selection**
     */
    private fun setupSendCommentButton() {
        // 🔥 Select Image from Gallery
        binding.addImageButton.setOnClickListener {
            imagePickerLauncher.launch("image/*") // Open gallery
        }

        // 🔥 When clicking on the preview image button, open the gallery again
        binding.previewImageButton.setOnClickListener {
            imagePickerLauncher.launch("image/*") // Open gallery again
        }

        // 🔥 Send Comment
        binding.sendCommentButton.setOnClickListener {
            val commentText =
                binding.commentInput.text
                    .toString()
                    .trim()

            if (commentText.isNotEmpty() || selectedImageUri != null) {
                viewLifecycleOwner.lifecycleScope.launch {
                    Log.d(TAG, "setupSendCommentButton: $selectedImageUri")

                    eventViewModel.addComment(eventId, commentText, selectedImageUri)

                    // ✅ Reset input field & image preview
                    clearImagePreview()
                    binding.commentInput.text.clear()
                    selectedImageUri = null
                }
            }
        }
    }

    private fun showImagePreview(imageUri: Uri) {
        binding.addImageButton.visibility = View.INVISIBLE // Hide Add Image Button (Keep space)
        binding.previewImageButtonCard.visibility = View.VISIBLE // Hide Preview
        binding.previewImageButton.visibility = View.VISIBLE // Show Preview Button
        binding.previewImageButton.isClickable = true // Enable Re-selection

        // Load selected image into preview button
        Picasso
            .get()
            .load(imageUri)
            .resize(100, 100)
            .centerCrop()
            .into(binding.previewImageButton)
    }

    /**
     * ✅ **Clear Image Preview and Restore Add Button**
     */
    private fun clearImagePreview() {
        binding.previewImageButtonCard.visibility = View.GONE // Hide Preview
        binding.previewImageButton.visibility = View.GONE // Hide Preview
        binding.previewImageButton.isClickable = false // Disable Click
        binding.addImageButton.visibility = View.VISIBLE // Restore Add Button
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingInternal = null
    }

    companion object {
        const val TAG = "EventCommentsModalBottomSheet"
    }
}
