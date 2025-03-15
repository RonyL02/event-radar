package com.col.eventradar.ui.bottom_sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.col.eventradar.data.local.EventRepository
import com.col.eventradar.databinding.FragmentEventCommentsBottomSheetBinding
import com.col.eventradar.models.common.Comment
import com.col.eventradar.ui.adapters.EventCommentRecyclerViewAdapter
import com.col.eventradar.ui.viewmodels.EventViewModel
import com.col.eventradar.ui.viewmodels.EventViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalDateTime

class EventCommentsBottomSheet(
    private val eventId: String,
) : BottomSheetDialogFragment() {
    private var bindingInternal: FragmentEventCommentsBottomSheetBinding? = null
    private val binding get() = bindingInternal!!
    private val eventViewModel: EventViewModel by activityViewModels {
        val repository = EventRepository(requireContext())
        EventViewModelFactory(repository)
    }
    private lateinit var commentRecyclerAdapter: EventCommentRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bindingInternal =
            FragmentEventCommentsBottomSheetBinding.inflate(inflater, container, false)

        binding.closeButton.setOnClickListener { dismiss() }

        setupRecyclerView()
        observeViewModel()
        setupSendCommentButton()

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
     * ✅ **Handle adding comments**
     */
    private fun setupSendCommentButton() {
        binding.addImageButton.setOnClickListener {
            val commentText =
                binding.commentInput.text
                    .toString()
                    .trim()

            if (commentText.isNotEmpty()) {
                val newComment =
                    Comment(
                        time = LocalDateTime.now(),
                    )

                eventViewModel.addComment(eventId, newComment) // ✅ Add comment

                binding.commentInput.text.clear() // Clear input field
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingInternal = null
    }

    companion object {
        const val TAG = "EventCommentsModalBottomSheet"
    }
}
