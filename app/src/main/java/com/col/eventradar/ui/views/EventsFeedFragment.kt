package com.col.eventradar.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.col.eventradar.data.repository.CommentsRepository
import com.col.eventradar.data.repository.EventRepository
import com.col.eventradar.databinding.FragmentEventsFeedBinding
import com.col.eventradar.ui.viewmodels.EventViewModel
import com.col.eventradar.ui.viewmodels.EventViewModelFactory

/**
 * A simple [Fragment] subclass.
 * Use the [EventsFeedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EventsFeedFragment : Fragment() {
    private var bindingInternal: FragmentEventsFeedBinding? = null
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
        bindingInternal = FragmentEventsFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        eventViewModel.events.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun refreshData() {
        binding.swipeRefreshLayout.isRefreshing = true
        eventViewModel.fetchFilteredEvents()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingInternal = null
    }
}
