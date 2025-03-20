package com.col.eventradar.ui.views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.col.eventradar.constants.EventTypeConfig
import com.col.eventradar.data.repository.CommentsRepository
import com.col.eventradar.data.repository.EventRepository
import com.col.eventradar.databinding.FragmentEventsStatsBinding
import com.col.eventradar.models.common.EventType
import com.col.eventradar.ui.viewmodels.EventViewModel
import com.col.eventradar.ui.viewmodels.EventViewModelFactory
import com.col.eventradar.utils.getShortFormattedDate
import java.time.LocalDateTime

/**
 * A simple [Fragment] subclass.
 * Use the [EventsStatsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EventsStatsFragment : Fragment() {
    private var bindingInternal: FragmentEventsStatsBinding? = null
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
        bindingInternal = FragmentEventsStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel() {
        val sinceOneYearAgo = LocalDateTime.now().minusYears(1)

        eventViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                startShimmer()
            } else {
                stopShimmerAndShowContent()
            }
        }

        eventViewModel.getEventsSince(sinceOneYearAgo).observe(viewLifecycleOwner) { total ->
            binding.totalEventsAmount.text = total.toString()
            binding.sinceTime.text = sinceOneYearAgo.getShortFormattedDate()
        }

        eventViewModel
            .getEventsPerTypeSince(sinceOneYearAgo)
            .observe(viewLifecycleOwner) { eventsPerType ->
                updateEventTypeUI(eventsPerType)
            }
    }

    private fun startShimmer() {
        binding.shimmerContainer.startShimmer()
        binding.shimmerContainer.visibility = View.VISIBLE
        binding.banner.visibility = View.GONE
    }

    private fun stopShimmerAndShowContent() {
        binding.shimmerContainer.stopShimmer()
        binding.shimmerContainer.visibility = View.GONE
        binding.banner.visibility = View.VISIBLE
    }

    private fun updateEventTypeUI(eventsPerType: Map<EventType, Int>) {
        Log.d(TAG, "Events per type: $eventsPerType")
        val sortedEvents = eventsPerType.entries.sortedByDescending { it.value }.take(2)

        if (sortedEvents.isEmpty()) {
            binding.topEventTitle.text = ""
            binding.topEventIcon.setImageDrawable(null)
            binding.topEventAmount.text = "0"

            binding.secondEventTitle.text = ""
            binding.secondEventIcon.setImageDrawable(null)
            binding.secondEventAmount.text = "0"
            return
        }

        if (sortedEvents.size >= 1) {
            val (firstType, firstCount) = sortedEvents[0]
            binding.topEventTitle.text = EventTypeConfig.getName(firstType)
            binding.topEventIcon.setImageResource(EventTypeConfig.getIconResId(firstType))
            binding.topEventAmount.text = firstCount.toString()
        }

        if (sortedEvents.size >= 2) {
            val (secondType, secondCount) = sortedEvents[1]
            binding.secondEventTitle.text = EventTypeConfig.getName(secondType)
            binding.secondEventIcon.setImageResource(EventTypeConfig.getIconResId(secondType))
            binding.secondEventAmount.text = secondCount.toString()
        }
    }

    companion object {
        const val TAG = "EventsStatsFragment"
    }
}
