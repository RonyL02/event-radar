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
import com.col.eventradar.models.EventModel

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
