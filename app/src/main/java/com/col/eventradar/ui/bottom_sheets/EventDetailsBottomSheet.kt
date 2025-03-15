package com.col.eventradar.ui.bottom_sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.col.eventradar.databinding.FragmentEventDetailsBottomSheetBinding
import com.col.eventradar.models.common.Event
import com.col.eventradar.models.common.getTitlePreview
import com.col.eventradar.utils.getFormattedDate
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class EventDetailsBottomSheet(
    private val event: Event,
) : BottomSheetDialogFragment() {
    private var bindingInternal: FragmentEventDetailsBottomSheetBinding? = null
    private val binding get() = bindingInternal!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bindingInternal = FragmentEventDetailsBottomSheetBinding.inflate(inflater, container, false)

        event.let {
            with(binding) {
                locationName.text = it.locationName
                eventTitle.text = it.getTitlePreview()
                eventDescription.text = it.description
                eventTime.text =
                    it.time.getFormattedDate()
                commentsCount.text = event.comments.size.toString()
                closeButton.setOnClickListener {
                    dismiss()
                }

                footer.setOnClickListener {
                    val modalBottomSheet = EventCommentsBottomSheet(event.id)
                    modalBottomSheet.show(parentFragmentManager, EventCommentsBottomSheet.TAG)
                }
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
