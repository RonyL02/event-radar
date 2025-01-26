package com.col.eventradar.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.col.eventradar.databinding.EventCommentRowBinding
import com.col.eventradar.models.Comment


class EventCommentRecyclerViewAdapter(
    private val comments: List<Comment>
) :
    RecyclerView.Adapter<EventCommentRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder = ViewHolder(
        EventCommentRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        ),
    )

    override fun getItemCount(): Int = comments.size

    override fun onBindViewHolder(
        holder: EventCommentRecyclerViewAdapter.ViewHolder,
        position: Int
    ) {
        val comment = comments[position]
        holder.apply {
            usernameView.text = comment.username
            contentView.text = comment.content
            timeView.text = "${comment.time.hour}:${comment.time.minute}"
        }
    }

    inner class ViewHolder(
        binding: EventCommentRowBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        val timeView = binding.commentTime
        val usernameView = binding.username
        val contentView = binding.content
    }
}