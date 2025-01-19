package com.col.eventradar.ui.bottom_sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.col.eventradar.databinding.FragmentEventDetailsBottmSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

data class EventDetails(
    val type: Int,
    val name: String,
    val locationName: String,
    val time: String,
    val description: String,
    val commentsAmount: Int,
)

class EventDetailsBottomSheet(private val eventDetails: EventDetails) :
    BottomSheetDialogFragment() {
    private var bindingInternal: FragmentEventDetailsBottmSheetBinding? = null
    private val binding get() = bindingInternal!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bindingInternal = FragmentEventDetailsBottmSheetBinding.inflate(inflater, container, false)

        binding.apply {
            eventTitle.text = eventDetails.name
            eventDescription.text = eventDetails.description
            time.text = eventDetails.time
            commentsCount.text = eventDetails.commentsAmount.toString()
            closeButton.setOnClickListener {
                dismiss()
            }
            addCommentButton.setOnClickListener {
                val commentText = commentInput.text
                //TODO: send commentText to db and image (if exists)
            }
            commentsTitle.setOnClickListener {
                val modalBottomSheet = EventCommentsBottomSheet()
                modalBottomSheet.show(parentFragmentManager, EventCommentsBottomSheet.TAG)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingInternal = null
    }

    companion object {
        const val TAG = "EventDetailsModalBottomSheet"
    }
}