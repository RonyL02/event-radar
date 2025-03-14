package com.col.eventradar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.col.eventradar.constants.EventTypeConfig
import com.col.eventradar.databinding.FragmentEventCardBinding
import com.col.eventradar.databinding.FragmentEventCardShimmerBinding
import com.col.eventradar.models.Event
import com.col.eventradar.models.getDescriptionPreview
import com.col.eventradar.models.getTitlePreview
import com.col.eventradar.utils.getFormattedDate

class EventCardRecyclerViewAdapter(
    private var events: List<Event> = emptyList(),
    private var isLoading: Boolean = true,
    private val onClickListener: (Event) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemViewType(position: Int): Int = if (isLoading) VIEW_TYPE_SHIMMER else VIEW_TYPE_ITEM

    fun setLoading(isLoading: Boolean) {
        this.isLoading = isLoading
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_SHIMMER) {
            ShimmerViewHolder(
                FragmentEventCardShimmerBinding.inflate(inflater, parent, false),
            )
        } else {
            EventViewHolder(
                FragmentEventCardBinding.inflate(inflater, parent, false),
            )
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        if (holder is EventViewHolder && !isLoading) {
            holder.bind(events[position])
        }
    }

    override fun getItemCount(): Int = if (isLoading) LOADING_SHIMMER_COUNT else events.size

    fun updateEvents(newEvents: List<Event>) {
        isLoading = false
        events = newEvents
        notifyDataSetChanged()
    }

    inner class EventViewHolder(
        private val binding: FragmentEventCardBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            with(binding) {
                eventTitle.text = event.getTitlePreview()
                eventTime.text = event.time.getFormattedDate()
                locationName.text = event.locationName
                eventTypeIcon.setImageResource(EventTypeConfig.getIconResId(event.type))
                eventDescription.text = event.getDescriptionPreview()
                commentsCount.text = event.comments.size.toString()

                root.setOnClickListener { onClickListener(event) }
            }
        }
    }

    inner class ShimmerViewHolder(
        binding: FragmentEventCardShimmerBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val LOADING_SHIMMER_COUNT = 5
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_SHIMMER = 1
    }
}
