package com.col.eventradar.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.col.eventradar.adapter.EventCardRecyclerViewAdapter
import com.col.eventradar.databinding.FragmentEventCardListBinding
import com.col.eventradar.models.EventDetails
import com.col.eventradar.models.EventModel
import com.col.eventradar.models.EventType
import com.col.eventradar.ui.bottom_sheets.EventDetailsBottomSheet
import java.time.LocalDateTime

/**
 * A fragment representing a list of Items.
 */
class EventCardListFragment : Fragment() {
    private var columnCount = 1
    private var bindingInternal: FragmentEventCardListBinding? = null

    /**
     * This property is only valid between `onCreateView` and `onDestroyView`.
     */
    private val binding get() = bindingInternal!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bindingInternal = FragmentEventCardListBinding.inflate(inflater, container, false)

        // Set the adapter
        with(binding.root) {
            layoutManager =
                when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
            adapter = EventCardRecyclerViewAdapter(EventModel.EVENTS_DATA)
        }

        val modalBottomSheet = EventDetailsBottomSheet(
            EventDetails(
                EventType.EarthQuake, "Earthquake", "Jerusalem",
                LocalDateTime.of(2022, 7, 7, 18, 58),
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. ",
                5
            )
        )
        modalBottomSheet.show(parentFragmentManager, EventDetailsBottomSheet.TAG)

        return binding.root
    }

    companion object {
        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            EventCardListFragment().apply {
                arguments =
                    Bundle().apply {
                        putInt(ARG_COLUMN_COUNT, columnCount)
                    }
            }
    }
}
