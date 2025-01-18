package com.col.eventradar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.col.eventradar.databinding.FragmentEventCardBinding
import com.col.eventradar.models.Event

/**
 * [RecyclerView.Adapter] that can display a [Event].
 * TODO: Replace the implementation with code for your data type.
 */
class EventCardRecyclerViewAdapter(
    private val values: List<Event>,
) : RecyclerView.Adapter<EventCardRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder =
        ViewHolder(
            FragmentEventCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val item = values[position]
        holder.idView.text = item.id
        holder.contentView.text = item.content
        holder.titleView.text = item.title
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(
        binding: FragmentEventCardBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemNumber
        val contentView: TextView = binding.eventFragmentContent
        val titleView: TextView = binding.eventFragmentTitle

        override fun toString(): String = super.toString() + " '" + titleView.text + contentView.text + "'"
    }
}
