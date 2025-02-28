package com.col.eventradar.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.col.eventradar.R
import com.col.eventradar.adapter.EventCardRecyclerViewAdapter
import com.col.eventradar.databinding.FragmentEventCardListBinding
import com.col.eventradar.ui.bottom_sheets.EventDetailsBottomSheet
import com.col.eventradar.viewmodel.EventViewModel
import java.time.LocalDateTime

/**
 * A fragment representing a list of Items.
 */
class EventCardListFragment : Fragment() {
    private var columnCount = 1
    private var bindingInternal: FragmentEventCardListBinding? = null
    private val eventViewModel: EventViewModel by activityViewModels()
    private lateinit var eventAdapter: EventCardRecyclerViewAdapter

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

        setupRecyclerView()
        observeViewModel()
        fetchEvents()

        return binding.root
    }

    private fun setupRecyclerView() {
        eventAdapter =
            EventCardRecyclerViewAdapter(
                emptyList(),
                onClickListener = { eventDetails ->
                    val modalBottomSheet = EventDetailsBottomSheet(eventDetails)
                    modalBottomSheet.show(parentFragmentManager, EventDetailsBottomSheet.TAG)
                },
            )

        with(binding.fragmentEventCardList) {
            layoutManager =
                if (columnCount <= 1) {
                    LinearLayoutManager(context)
                } else {
                    GridLayoutManager(context, columnCount)
                }
            adapter = eventAdapter

            val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            ContextCompat.getDrawable(requireContext(), R.drawable.event_list_divider)?.let {
                divider.setDrawable(it)
            }
            addItemDecoration(divider)
        }
    }

    private fun observeViewModel() {
        eventViewModel.events.observe(viewLifecycleOwner) { events ->
            eventAdapter.updateEvents(events)
        }
    }

    private fun fetchEvents() {
        eventViewModel.fetchFilteredEvents(
            fromDate = LocalDateTime.now().minusMonths(12),
            toDate = LocalDateTime.now(),
        )
    }

    companion object {
        const val TAG = "EventCardListFragment"

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
