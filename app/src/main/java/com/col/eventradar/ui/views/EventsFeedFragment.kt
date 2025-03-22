package com.col.eventradar.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.col.eventradar.data.local.AreasOfInterestRepository
import com.col.eventradar.data.remote.UserRepository
import com.col.eventradar.data.repository.CommentsRepository
import com.col.eventradar.data.repository.EventRepository
import com.col.eventradar.databinding.FragmentEventsFeedBinding
import com.col.eventradar.ui.viewmodels.EventViewModel
import com.col.eventradar.ui.viewmodels.EventViewModelFactory
import com.col.eventradar.ui.viewmodels.UserViewModel
import com.col.eventradar.ui.viewmodels.UserViewModelFactory

class EventsFeedFragment : Fragment() {
    private var bindingInternal: FragmentEventsFeedBinding? = null
    private val binding get() = bindingInternal!!

    private val eventViewModel: EventViewModel by activityViewModels {
        val eventRepository = EventRepository(requireContext())
        val commentRepository = CommentsRepository(requireContext())
        EventViewModelFactory(eventRepository, commentRepository)
    }

    private val userViewModel: UserViewModel by activityViewModels {
        val userRepository = UserRepository(requireContext())
        val commentRepository = CommentsRepository(requireContext())
        val areasRepository = AreasOfInterestRepository(requireContext())
        UserViewModelFactory(commentRepository, userRepository, areasRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bindingInternal = FragmentEventsFeedBinding.inflate(inflater, container, false)

        userViewModel.observeAndSyncUserAreas()

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupUiState()
        observeUserInterests()
        observeEventViewModel()
    }

    override fun onResume() {
        super.onResume()
        setupUiState()
        userViewModel.checkUserStatus()
    }

    private fun setupUiState() =
        with(binding) {
            loadingProgressBar.visibility = View.VISIBLE
            swipeRefreshLayout.visibility = View.GONE
            emptyStateContainer.visibility = View.GONE

            swipeRefreshLayout.setOnRefreshListener {
                refreshData()
            }

            addInterestAreasButton.setOnClickListener {
                val action = EventsFeedFragmentDirections.actionNavigationHomeToNavigationMap()
                findNavController().navigate(action)
            }
        }

    private fun observeEventViewModel() {
        eventViewModel.events.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun observeUserInterests() {
        userViewModel.loggedInUser.observe(viewLifecycleOwner) { user ->
            with(binding) {
                loadingProgressBar.visibility = View.GONE

                if (user?.areasOfInterest.isNullOrEmpty()) {
                    emptyStateContainer.visibility = View.VISIBLE
                    swipeRefreshLayout.visibility = View.GONE
                } else {
                    emptyStateContainer.visibility = View.GONE
                    swipeRefreshLayout.visibility = View.VISIBLE
                }
            }
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
