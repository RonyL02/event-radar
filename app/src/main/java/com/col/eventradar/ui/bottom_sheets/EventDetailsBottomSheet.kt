package com.col.eventradar.ui.bottom_sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.col.eventradar.databinding.FragmentEventDetailsBottomSheetBinding
import com.col.eventradar.models.EventDetails
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class EventDetailsBottomSheet(private val eventDetails: EventDetails) :
    BottomSheetDialogFragment() {
    private var bindingInternal: FragmentEventDetailsBottomSheetBinding? = null
    private val binding get() = bindingInternal!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bindingInternal = FragmentEventDetailsBottomSheetBinding.inflate(inflater, container, false)

        binding.apply {
            locationName.text = eventDetails.locationName
            eventTitle.text = eventDetails.name
            eventDescription.text = eventDetails.description
            time.text = "${eventDetails.time.hour}:${eventDetails.time.minute}"
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