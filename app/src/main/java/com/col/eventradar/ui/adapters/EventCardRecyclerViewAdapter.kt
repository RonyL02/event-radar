package com.col.eventradar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.col.eventradar.databinding.FragmentEventCardBinding
import com.col.eventradar.models.Event
import com.col.eventradar.models.EventTypeConfig
import com.col.eventradar.models.getDescriptionPreview
import com.col.eventradar.models.getTitlePreview
import com.col.eventradar.utils.getFormattedDate

/**
 * [RecyclerView.Adapter] that can display a [Event].
 * TODO: Replace the implementation with code for your data type.
 */
class EventCardRecyclerViewAdapter(
    private var events: List<Event>,
    private val onClickListener: (Event) -> Unit,
) : RecyclerView.Adapter<EventCardRecyclerViewAdapter.EventViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = EventViewHolder(
        FragmentEventCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        ),
    )

    override fun onBindViewHolder(
        holder: EventViewHolder,
        position: Int,
    ) = holder.bind(events[position])

    override fun getItemCount(): Int = events.size

    fun updateEvents(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }

    inner class EventViewHolder(
        private val binding: FragmentEventCardBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            with(binding) {
                eventTitle.text = event.getTitlePreview()
                eventTime.text =
                    event.time.getFormattedDate()
                locationName.text = event.locationName
                eventTypeIcon.setImageResource(EventTypeConfig.getIconResId(event.type))
                eventDescription.text = event.getDescriptionPreview()
                commentsCount.text = event.comments.size.toString()

                root.setOnClickListener { onClickListener(event) }
            }
        }
    }
}
