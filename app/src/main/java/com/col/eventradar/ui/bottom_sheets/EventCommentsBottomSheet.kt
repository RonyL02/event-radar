package com.col.eventradar.ui.bottom_sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.col.eventradar.databinding.FragmentEventCommentsBottomSheetBinding
import com.col.eventradar.models.Comment
import com.col.eventradar.ui.adapters.EventCommentRecyclerViewAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalDateTime
import java.time.Month

class EventCommentsBottomSheet :
    BottomSheetDialogFragment() {
    private var bindingInternal: FragmentEventCommentsBottomSheetBinding? = null
    private val binding get() = bindingInternal!!
    private lateinit var commentRecyclerAdapter: EventCommentRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bindingInternal =
            FragmentEventCommentsBottomSheetBinding.inflate(inflater, container, false)

        binding.closeButton.setOnClickListener {
            dismiss()
        }

        binding.commentsList.apply {
            setHasFixedSize(true)

            val linearLayoutManager = LinearLayoutManager(context)
            layoutManager = linearLayoutManager
            commentRecyclerAdapter =
                EventCommentRecyclerViewAdapter(
                    listOf(
                        Comment(
                            "sdfsfsfddsfdsfdsfdsd",
                            "sdfsdf",
                            LocalDateTime.of(2025, Month.MAY, 12, 5, 23)
                        ),
                        Comment(
                            "sdfsfsfddsfdsfdsfdsd", "sdfsdf",
                            LocalDateTime.of(2025, Month.MAY, 12, 5, 23)
                        ),
                        Comment(
                            "aaaaaaaaa", "sdfsdf",
                            LocalDateTime.of(2025, Month.MAY, 12, 5, 23)
                        ),
                        Comment(
                            "sdfsfsfddsfdsfdsfdsd", "sdfsdf",
                            LocalDateTime.of(2025, Month.MAY, 12, 5, 23)
                        ),
                        Comment(
                            "sdfsfsfddsfdsfdsfdsd", "sdfsdf",
                            LocalDateTime.of(2025, Month.MAY, 12, 5, 23)
                        ),
                        Comment(
                            "sdfsfsfddsfdsfdsfdsd", "sdfsdf",
                            LocalDateTime.of(2025, Month.MAY, 12, 5, 23)
                        ),
                        Comment(
                            "sdfsfsfddsfdsfdsfdsd", "sdfsdf",
                            LocalDateTime.of(2025, Month.MAY, 12, 5, 23)
                        ),
                        Comment(
                            "sdfsfsfddsfdsfdsfdsd", "sdfsdf",
                            LocalDateTime.of(2025, Month.MAY, 12, 5, 23)
                        ),
                        Comment(
                            "sdfsfsfddsfdsfdsfdsd", "sdfsdf",
                            LocalDateTime.of(2025, Month.MAY, 12, 5, 23)
                        ),
                        Comment(
                            "sdfsfsfddsfdsfdsfdsd", "sdfsdf",
                            LocalDateTime.of(2025, Month.MAY, 12, 5, 23)
                        ),
                        Comment(
                            "sdfsfsfddsfdsfdsfdsd", "sdfsdf",
                            LocalDateTime.of(2025, Month.MAY, 12, 5, 23)
                        ),
                        Comment(
                            "sdfsfsfddsfdsfdsfdsd", "sdfsdf",
                            LocalDateTime.of(2025, Month.MAY, 12, 5, 23)
                        ),
                        Comment(
                            "sdfsfsfddsfdsfdsfdsd", "sdfsdf",
                            LocalDateTime.of(2025, Month.MAY, 12, 5, 23)
                        ),
                        Comment(
                            "sdfsfsfddsfdsfdsfdsd", "sdfsdf",
                            LocalDateTime.of(2025, Month.MAY, 12, 5, 23)
                        )
                    )
                )
            adapter = commentRecyclerAdapter
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bindingInternal = null
    }

    companion object {
        const val TAG = "EventCommentsModalBottomSheet"
    }
}