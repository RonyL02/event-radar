package com.col.eventradar.ui.views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.col.eventradar.constants.EventTypeConfig
import com.col.eventradar.databinding.FragmentEventsStatsBinding
import com.col.eventradar.models.EventType
import com.col.eventradar.ui.viewmodels.EventViewModel
import com.col.eventradar.utils.getShortFormattedDate
import com.facebook.shimmer.ShimmerFrameLayout
import java.time.LocalDateTime

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EventsStatsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EventsStatsFragment : Fragment() {
    private var bindingInternal: FragmentEventsStatsBinding? = null
    private val binding get() = bindingInternal!!
    private val eventViewModel: EventViewModel by activityViewModels()
    private var shimmerLayout: ShimmerFrameLayout? = null

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
        val sinceOneMonthAgo = LocalDateTime.now().minusYears(1)

        eventViewModel.getEventsSince(sinceOneMonthAgo).observe(viewLifecycleOwner) { total ->
            binding.totalEventsAmount.text = total.toString()
            binding.sinceTime.text = sinceOneMonthAgo.getShortFormattedDate()
            stopShimmerAndShowContent()
        }

        eventViewModel
            .getEventsPerTypeSince(sinceOneMonthAgo)
            .observe(viewLifecycleOwner) { eventsPerType -> updateEventTypeUI(eventsPerType) }
    }

    private fun stopShimmerAndShowContent() {
        shimmerLayout?.stopShimmer()
        shimmerLayout?.visibility = View.GONE
        binding.shimmerContainer.visibility = View.GONE
        binding.banner.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingInternal = null
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
