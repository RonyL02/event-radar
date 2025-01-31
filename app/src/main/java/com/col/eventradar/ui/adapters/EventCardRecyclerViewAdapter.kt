package com.col.eventradar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.col.eventradar.databinding.FragmentEventCardBinding
import com.col.eventradar.models.Event

/**
 * [RecyclerView.Adapter] that can display a [Event].
 * TODO: Replace the implementation with code for your data type.
 */
class EventCardRecyclerViewAdapter(
    private val events: List<Event>,
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

    inner class EventViewHolder(
        private val binding: FragmentEventCardBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            with(binding) {
                eventTitle.text = event.title
                eventTime.text =
                    event.getFormattedTime()
                locationName.text = event.locationName
                eventTypeIcon.setImageResource(event.getIconRes())
                eventDescription.text = event.getDescriptionPreview()
                commentsCount.text = event.comments.size.toString()

                root.setOnClickListener { onClickListener(event) }
            }
        }
    }
}
