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
import com.col.eventradar.data.repository.CommentsRepository
import com.col.eventradar.data.repository.EventRepository
import com.col.eventradar.databinding.FragmentEventCardListBinding
import com.col.eventradar.ui.bottom_sheets.EventDetailsBottomSheet
import com.col.eventradar.ui.viewmodels.EventViewModel
import com.col.eventradar.ui.viewmodels.EventViewModelFactory

/**
 * A fragment representing a list of Items.
 */
class EventCardListFragment : Fragment() {
    private var columnCount = 1
    private var bindingInternal: FragmentEventCardListBinding? = null
    private val eventViewModel: EventViewModel by activityViewModels {
        val eventRepository = EventRepository(requireContext())
        val commentRepository = CommentsRepository(requireContext())
        EventViewModelFactory(eventRepository, commentRepository)
    }
    private lateinit var eventAdapter: EventCardRecyclerViewAdapter

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
                isLoading = true,
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
        eventViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            eventAdapter.setLoading(isLoading)
        }

        eventViewModel.events.observe(viewLifecycleOwner) { events ->
            eventAdapter.updateEvents(events)
        }
    }

    private fun fetchEvents() {
        eventViewModel.fetchFilteredEvents()
    }

    companion object {
        const val TAG = "EventCardListFragment"

        const val ARG_COLUMN_COUNT = "column-count"

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
