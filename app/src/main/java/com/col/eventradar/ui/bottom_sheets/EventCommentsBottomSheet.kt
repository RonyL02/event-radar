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
import com.col.eventradar.utils.ImageUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class EventCommentsBottomSheet(
    private val eventId: String,
) : BottomSheetDialogFragment() {
    private var bindingInternal: FragmentEventCommentsBottomSheetBinding? = null
    private val binding get() = bindingInternal!!

    private lateinit var commentsRepository: CommentsRepository
    private lateinit var commentRecyclerAdapter: EventCommentRecyclerViewAdapter

    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                Log.d(TAG, "Image Selected: $uri")
                selectedImageUri = uri
                showImagePreview(uri)
            } else {
                Log.e(TAG, "No image selected")
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
        binding.previewImageButtonCard.visibility = View.GONE

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val parentLayout = view.parent as View
        val bottomSheetBehavior = BottomSheetBehavior.from(parentLayout)
        bottomSheetBehavior.isDraggable = false

        eventViewModel.fetchComments(eventId)
    }

    private fun setupRecyclerView() {
        binding.commentsList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            commentRecyclerAdapter =
                EventCommentRecyclerViewAdapter(mutableListOf())
            adapter = commentRecyclerAdapter
        }
    }

    private fun observeViewModel() {
        eventViewModel.comments.observe(viewLifecycleOwner) { comments ->
            commentRecyclerAdapter.updateComments(comments)
        }

        eventViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.sendCommentButton.visibility = if (!isLoading) View.VISIBLE else View.GONE
            binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    /**
     * **Handle adding comments & Image Selection**
     */
    private fun setupSendCommentButton() {
        binding.addImageButton.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.previewImageButton.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.sendCommentButton.setOnClickListener {
            val commentText =
                binding.commentInput.text
                    .toString()
                    .trim()

            if (commentText.isNotEmpty() || selectedImageUri != null) {
                viewLifecycleOwner.lifecycleScope.launch {
                    eventViewModel.addComment(eventId, commentText, selectedImageUri)

                    clearImagePreview()
                    binding.commentInput.text.clear()
                    selectedImageUri = null
                }
            }
        }
    }

    private fun showImagePreview(imageUri: Uri) {
        binding.addImageButton.visibility = View.INVISIBLE
        binding.previewImageButtonCard.visibility = View.VISIBLE
        binding.previewImageButton.visibility = View.VISIBLE
        binding.previewImageButton.isClickable = true

        ImageUtils.loadImage(imageUri, binding.previewImageButton)
    }

    /**
     * **Clear Image Preview and Restore Add Button**
     */
    private fun clearImagePreview() {
        binding.previewImageButtonCard.visibility = View.GONE
        binding.previewImageButton.visibility = View.GONE
        binding.previewImageButton.isClickable = false
        binding.addImageButton.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingInternal = null
    }

    companion object {
        const val TAG = "EventCommentsModalBottomSheet"
    }
}
