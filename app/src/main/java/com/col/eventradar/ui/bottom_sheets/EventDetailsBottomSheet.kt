package com.col.eventradar.ui.bottom_sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.col.eventradar.constants.EventTypeConfig
import com.col.eventradar.data.repository.CommentsRepository
import com.col.eventradar.data.repository.EventRepository
import com.col.eventradar.databinding.FragmentEventDetailsBottomSheetBinding
import com.col.eventradar.models.common.Event
import com.col.eventradar.models.common.getTitlePreview
import com.col.eventradar.ui.viewmodels.EventViewModel
import com.col.eventradar.ui.viewmodels.EventViewModelFactory
import com.col.eventradar.utils.getFormattedDate
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class EventDetailsBottomSheet(
    private val event: Event,
) : BottomSheetDialogFragment() {
    private var bindingInternal: FragmentEventDetailsBottomSheetBinding? = null
    private val binding get() = bindingInternal!!
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
        bindingInternal = FragmentEventDetailsBottomSheetBinding.inflate(inflater, container, false)

        event.let {
            with(binding) {
                val iconResId = EventTypeConfig.getIconResId(it.type)
                eventTypeIcon.setImageResource(iconResId)
                locationName.text = it.locationName
                eventTitle.text = it.getTitlePreview()
                eventDescription.text = it.description
                eventTime.text =
                    it.time.getFormattedDate()
                commentsCount.text = event.comments.size.toString()

                footer.setOnClickListener {
                    val modalBottomSheet = EventCommentsBottomSheet(event.id)
                    modalBottomSheet.show(parentFragmentManager, EventCommentsBottomSheet.TAG)
                }
            }
        }

        observeCommentUpdates()

        return binding.root
    }

    private fun observeCommentUpdates() {
        eventViewModel.comments.observe(viewLifecycleOwner) { comments ->
            val filteredComments = comments.filter { it.eventId == event.id }
            binding.commentsCount.text =
                if (filteredComments.isEmpty()) event.comments.size.toString() else filteredComments.size.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingInternal = null
    }

    companion object {
        const val TAG = "EventDetailsModalBottomSheet"
    }
}
