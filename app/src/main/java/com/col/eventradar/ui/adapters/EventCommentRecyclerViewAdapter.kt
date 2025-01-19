package com.col.eventradar.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.col.eventradar.databinding.EventCommentBinding

data class Comment(val content: String, val username: String, val time: String)

class EventCommentRecyclerViewAdapter(
    private val values: List<Comment>
) :
    RecyclerView.Adapter<EventCommentRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder = ViewHolder(
        EventCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        ),
    )

    override fun getItemCount(): Int = values.size

    override fun onBindViewHolder(
        holder: EventCommentRecyclerViewAdapter.ViewHolder,
        position: Int
    ) {
        val item = values[position]
        holder.apply {
            usernameView.text = item.username
            contentView.text = item.content
            timeView.text = item.time
        }
    }

    inner class ViewHolder(
        binding: EventCommentBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        val timeView = binding.time
        val usernameView = binding.username
        val contentView = binding.content
    }
}