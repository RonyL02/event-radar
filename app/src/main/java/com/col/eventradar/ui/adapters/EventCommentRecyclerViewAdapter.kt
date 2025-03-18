package com.col.eventradar.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.col.eventradar.R
import com.col.eventradar.databinding.EventCommentRowBinding
import com.col.eventradar.models.common.Comment
import com.col.eventradar.utils.ImageUtils
import com.col.eventradar.utils.getTimeAgo

sealed class BaseViewHolder(
    itemView: View,
) : RecyclerView.ViewHolder(itemView) {
    class TextCommentViewHolder(
        val binding: EventCommentRowBinding,
    ) : BaseViewHolder(binding.root)

    class ImageCommentViewHolder(
        val binding: EventCommentRowBinding,
    ) : BaseViewHolder(binding.root)
}

class EventCommentRecyclerViewAdapter(
    private var comments: MutableList<Comment> = mutableListOf(),
) : RecyclerView.Adapter<BaseViewHolder>() {
    companion object {
        private const val TYPE_TEXT = 1
        private const val TYPE_IMAGE = 2
        private const val TAG = "EventCommentRecyclerViewAdapter"
    }

    override fun getItemViewType(position: Int): Int {
        val comment = comments[position]
        val type = if (comment.hasImage()) TYPE_IMAGE else TYPE_TEXT

        return type
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BaseViewHolder {
        val binding =
            EventCommentRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return when (viewType) {
            TYPE_IMAGE -> BaseViewHolder.ImageCommentViewHolder(binding)
            else -> BaseViewHolder.TextCommentViewHolder(binding)
        }
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder,
        position: Int,
    ) {
        val comment = comments[position]

        when (holder) {
            is BaseViewHolder.TextCommentViewHolder -> bindTextComment(holder, comment)
            is BaseViewHolder.ImageCommentViewHolder -> bindImageComment(holder, comment)
        }
    }

    private fun bindTextComment(
        holder: BaseViewHolder.TextCommentViewHolder,
        comment: Comment,
    ) {
        with(holder.binding) {
            val user = comment.user
            username.text = user?.username ?: "Unknown User"
            content.text = comment.content
            commentTime.text =
                comment.time.getTimeAgo()

            val userImageUrl = user?.imageUri
            if (!userImageUrl.isNullOrBlank()) {
                ImageUtils.showImgInViewFromUrl(userImageUrl, userImage, userImageLoader)
            } else {
                Log.e(TAG, "❌ User image is null, using default avatar")
                userImage.setImageResource(R.mipmap.ic_launcher_round)
            }

            commentImage.visibility = View.GONE
            commentImageCard.visibility = View.GONE
        }
    }

    private fun bindImageComment(
        holder: BaseViewHolder.ImageCommentViewHolder,
        comment: Comment,
    ) {
        with(holder.binding) {
            val user = comment.user
            username.text = user?.username ?: "Unknown User"
            content.text = comment.content
            commentTime.text =
                comment.time.getTimeAgo()

            val userImageUrl = user?.imageUri
            if (!userImageUrl.isNullOrBlank()) {
                ImageUtils.showImgInViewFromUrl(userImageUrl, userImage, userImageLoader)
            }

            if (!comment.imageUrl.isNullOrBlank()) {
                commentImage.visibility = View.VISIBLE
                ImageUtils.showImgInViewFromUrl(comment.imageUrl, commentImage, commentImageLoader)
            } else {
                Log.e(TAG, "❌ Comment image is null, hiding image view.")
                commentImage.setImageResource(R.mipmap.ic_launcher_round)
                commentImage.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = comments.size

    fun updateComments(newComments: List<Comment>) {
        Log.d(TAG, "updateComments: New List Size=${newComments.size}")
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }
}
