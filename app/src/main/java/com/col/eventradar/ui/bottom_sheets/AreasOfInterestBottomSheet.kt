package com.col.eventradar.ui.bottom_sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.col.eventradar.R
import com.col.eventradar.data.EventRepository
import com.col.eventradar.data.local.AreasOfInterestRepository
import com.col.eventradar.data.remote.UserRepository
import com.col.eventradar.data.repository.CommentsRepository
import com.col.eventradar.databinding.AreasOfInterestBottomSheetBinding
import com.col.eventradar.models.common.AreaOfInterest
import com.col.eventradar.models.common.User
import com.col.eventradar.ui.adapters.AreasOfInterestAdapter
import com.col.eventradar.ui.viewmodels.AreasViewModel
import com.col.eventradar.ui.viewmodels.AreasViewModelFactory
import com.col.eventradar.ui.viewmodels.UserViewModel
import com.col.eventradar.ui.viewmodels.UserViewModelFactory
import com.col.eventradar.ui.views.NoLastDividerItemDecoration
import com.col.eventradar.ui.views.SettingsFragmentDirections
import com.col.eventradar.utils.UserAreaManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class AreasOfInterestBottomSheet : BottomSheetDialogFragment() {
    private var bindingInternal: AreasOfInterestBottomSheetBinding? = null
    private val binding get() = bindingInternal!!

    private var currentUser: User? = null

    private val areasViewModel: AreasViewModel by activityViewModels {
        val repository = AreasOfInterestRepository(requireContext())
        AreasViewModelFactory(repository)
    }

    private val userViewModel: UserViewModel by activityViewModels {
        val repository = UserRepository(requireContext())
        val commentRepository = CommentsRepository(requireContext())
        UserViewModelFactory(commentRepository, repository)
    }

    private val areasOfInterestAdapter =
        AreasOfInterestAdapter(
            onRemove = { area ->
                lifecycleScope.launch {
                    if (currentUser != null) {
                        UserAreaManager(
                            UserRepository(requireContext()),
                            EventRepository(requireContext()),
                            AreasOfInterestRepository(requireContext()),
                        ).removeAreaOfInterest(
                            currentUser!!.id,
                            AreaOfInterest(
                                area.placeId,
                                area.name,
                                area.country,
                            ),
                        )
                        areasViewModel.removeArea(area.placeId)
                        AreasOfInterestRepository(requireContext()).deleteFeature(area.placeId)
                    }
                }
            },
        )


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bindingInternal = AreasOfInterestBottomSheetBinding.inflate(inflater, container, false)

        binding.apply {
            with(areasList) {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = areasOfInterestAdapter
                val divider =
                    NoLastDividerItemDecoration(
                        requireContext(),
                        RecyclerView.VERTICAL,
                        R.drawable.event_list_divider,
                    )
                areasList.addItemDecoration(divider)
            }
            addInterestAreasButton.setOnClickListener {
                dismiss()
                val action =
                    SettingsFragmentDirections.actionNavigationSettingsToNavigationMap()
                findNavController().navigate(action)
            }
        }
        observeViewModel()
        return binding.root
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            userViewModel.user.collect { user ->
                if (user != currentUser && user != null) {
                    currentUser = user
                }
            }
        }

        areasViewModel.areasLiveData.observe(viewLifecycleOwner) { features ->
            areasOfInterestAdapter.submitList(features)
            if (features.isNotEmpty()) {
                binding.apply {
                    areasList.visibility = View.VISIBLE
                    addInterestAreasButton.visibility = View.GONE
                }
            } else {
                binding.apply {
                    areasList.visibility = View.GONE
                    addInterestAreasButton.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingInternal = null
    }

    companion object {
        const val TAG = "AreasOfInterestBottomSheet"
    }
}