package com.col.eventradar.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.col.eventradar.databinding.FragmentEventsStatsBinding
import com.col.eventradar.models.EventModel.getEventsAmountPerTypeSince
import com.col.eventradar.models.EventModel.getTotalEventsAmountSince
import com.col.eventradar.models.EventType
import com.col.eventradar.models.EventTypeConfig
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bindingInternal = FragmentEventsStatsBinding.inflate(inflater, container, false)

        val sinceTime = LocalDateTime.now().minusHours(5)

        val totalEventsAmount = getTotalEventsAmountSince(sinceTime)
        val eventsAmountPerType = getEventsAmountPerTypeSince(sinceTime)

        binding.totalEventsAmount.text = totalEventsAmount.toString()

        binding.type1Title.text = EventTypeConfig.getName(EventType.Disaster)
        binding.type1Icon.setImageResource(EventTypeConfig.getIconResId(EventType.Disaster))
        binding.type1EventsAmount.text = eventsAmountPerType[EventType.Disaster].toString()

        binding.type2Title.text = EventTypeConfig.getName(EventType.Suicide)
        binding.type2Icon.setImageResource(EventTypeConfig.getIconResId(EventType.Suicide))
        binding.type2EventsAmount.text = eventsAmountPerType[EventType.Suicide].toString()

        binding.sinceTime.text = sinceTime.getFormattedTime()

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
