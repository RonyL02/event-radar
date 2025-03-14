package com.col.eventradar.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.col.eventradar.databinding.EventCommentRowBinding
import com.col.eventradar.models.Comment
import java.util.Locale

class EventCommentRecyclerViewAdapter(
    private val comments: List<Comment>,
) : RecyclerView.Adapter<EventCommentRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = ViewHolder(
        EventCommentRowBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun getItemCount() = comments.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) = holder.bind(comments[position])

    inner class ViewHolder(
        private val binding: EventCommentRowBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) =
            with(binding) {
                username.text = comment.username
                content.text = comment.content
                commentTime.text =
                    String.format(Locale.UK, "%02d:%02d", comment.time.hour, comment.time.minute)
            }
    }
}
