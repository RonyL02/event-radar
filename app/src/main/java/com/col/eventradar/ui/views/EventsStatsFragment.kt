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
import com.col.eventradar.utils.getFormattedTime
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
        val sinceOneMonthAgo = LocalDateTime.now().minusMonths(12)

        eventViewModel.getEventsSince(sinceOneMonthAgo).observe(viewLifecycleOwner) { total ->
            binding.totalEventsAmount.text = total.toString()
        }

        eventViewModel
            .getEventsPerTypeSince(sinceOneMonthAgo)
            .observe(viewLifecycleOwner) { eventsPerType ->
                updateEventTypeUI(EventType.FLOOD, eventsPerType)
                updateEventTypeUI(EventType.EARTHQUAKE, eventsPerType)
                updateEventTypeUI(EventType.FOREST_FIRE, eventsPerType)
            }

        binding.sinceTime.text = LocalDateTime.now().minusHours(5).getFormattedTime()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingInternal = null
    }

    private fun updateEventTypeUI(
        type: EventType,
        eventsPerType: Map<EventType, Int>,
    ) {
        val titleView =
            when (type) {
                EventType.DROUGHT -> binding.type1Title
                EventType.FOREST_FIRE -> binding.type2Title
                else -> return
            }
        val iconView =
            when (type) {
                EventType.DROUGHT -> binding.type1Icon
                EventType.FOREST_FIRE -> binding.type2Icon
                else -> return
            }
        val amountView =
            when (type) {
                EventType.DROUGHT -> binding.type1EventsAmount
                EventType.FOREST_FIRE -> binding.type2EventsAmount
                else -> return
            }

        Log.d(TAG, "updateEventTypeUI: ${eventsPerType[type]}")

        titleView.text = EventTypeConfig.getName(type)
        iconView.setImageResource(EventTypeConfig.getIconResId(type))
        amountView.text = eventsPerType[type]?.toString() ?: "0"
    }

    companion object {
        const val TAG = "EventsStatsFragment"
    }
}
