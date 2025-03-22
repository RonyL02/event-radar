package com.col.eventradar.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.col.eventradar.data.local.AreasOfInterestRepository
import com.col.eventradar.data.remote.UserRepository
import com.col.eventradar.data.repository.CommentsRepository
import com.col.eventradar.databinding.FragmentCommentsBinding
import com.col.eventradar.ui.adapters.UserCommentsRecyclerViewAdapter
import com.col.eventradar.ui.bottom_sheets.EditCommentBottomSheetFragment
import com.col.eventradar.ui.components.ToastFragment
import com.col.eventradar.ui.viewmodels.UserViewModel
import com.col.eventradar.ui.viewmodels.UserViewModelFactory

class CommentsFragment : Fragment() {
    private var bindingInternal: FragmentCommentsBinding? = null
    private val binding get() = bindingInternal!!
    private lateinit var toastFragment: ToastFragment

    private val userViewModel: UserViewModel by activityViewModels {
        val userRepository = UserRepository(requireContext())
        val commentRepository = CommentsRepository(requireContext())
        val areasRepository = AreasOfInterestRepository(requireContext())
        UserViewModelFactory(commentRepository, userRepository, areasRepository)
    }

    private lateinit var commentsAdapter: UserCommentsRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bindingInternal = FragmentCommentsBinding.inflate(inflater, container, false)
        toastFragment = ToastFragment()
        childFragmentManager.beginTransaction().add(toastFragment, ToastFragment.TAG).commit()

        setupRecyclerView()
        observeViewModel()
        fetchUserComments()

        return binding.root
    }

    private fun setupRecyclerView() {
        commentsAdapter =
            UserCommentsRecyclerViewAdapter(
                mutableListOf(),
                onEditClick = { populatedComment ->
                    val modalBottomSheet =
                        EditCommentBottomSheetFragment(populatedComment) {
                            toastFragment("Comment Updated")
                            fetchUserComments()
                        }
                    modalBottomSheet.show(parentFragmentManager, EditCommentBottomSheetFragment.TAG)
                },
                onDeleteClick = { populatedComment ->
                    userViewModel.deleteCommentById(populatedComment.comment.id)
                },
            )

        with(binding.userCommentsList) {
            layoutManager = LinearLayoutManager(context)
            adapter = commentsAdapter
        }
    }

    private fun observeViewModel() {
        userViewModel.userComments.observe(viewLifecycleOwner) { populatedComments ->
            commentsAdapter.updateComments(populatedComments)
        }
    }

    private fun fetchUserComments() {
        userViewModel.fetchLoggedInUserComments()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingInternal = null
    }

    companion object {
        const val TAG = "CommentsFragment"
    }
}
