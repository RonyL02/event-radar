package com.col.eventradar.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.col.eventradar.constants.EventTypeConfig
import com.col.eventradar.databinding.UserCommentRowBinding
import com.col.eventradar.models.common.PopulatedComment
import com.col.eventradar.utils.ImageUtils
import com.col.eventradar.utils.getFormattedDate
import com.col.eventradar.utils.getLongTimeAgo

sealed class BaseUserCommentViewHolder(
    itemView: View,
) : RecyclerView.ViewHolder(itemView) {
    class TextCommentViewHolder(
        val binding: UserCommentRowBinding,
    ) : BaseUserCommentViewHolder(binding.root)

    class ImageCommentViewHolder(
        val binding: UserCommentRowBinding,
    ) : BaseUserCommentViewHolder(binding.root)
}

class UserCommentsRecyclerViewAdapter(
    private var comments: MutableList<PopulatedComment> = mutableListOf(),
    private val onEditClick: (PopulatedComment) -> Unit,
    private val onDeleteClick: (PopulatedComment) -> Unit,
) : RecyclerView.Adapter<BaseUserCommentViewHolder>() {
    companion object {
        private const val TYPE_TEXT_COMMENT = 1
        private const val TYPE_IMAGE_COMMENT = 2
        private const val TAG = "UserCommentsRecyclerViewAdapter"
    }

    override fun getItemViewType(position: Int): Int = if (comments[position].comment.hasImage()) TYPE_IMAGE_COMMENT else TYPE_TEXT_COMMENT

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BaseUserCommentViewHolder {
        val binding =
            UserCommentRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return when (viewType) {
            TYPE_IMAGE_COMMENT -> BaseUserCommentViewHolder.ImageCommentViewHolder(binding)
            else -> BaseUserCommentViewHolder.TextCommentViewHolder(binding)
        }
    }

    override fun onBindViewHolder(
        holder: BaseUserCommentViewHolder,
        position: Int,
    ) {
        val populatedComment = comments[position]

        when (holder) {
            is BaseUserCommentViewHolder.TextCommentViewHolder ->
                bindTextComment(holder, populatedComment)

            is BaseUserCommentViewHolder.ImageCommentViewHolder ->
                bindImageComment(holder, populatedComment)
        }
    }

    /**
     * **Bind Common Fields for Both Text & Image Comments**
     */
    private fun bindCommonFields(
        binding: UserCommentRowBinding,
        populatedComment: PopulatedComment,
    ) {
        val comment = populatedComment.comment
        val event = populatedComment.event
        val iconResId = EventTypeConfig.getIconResId(event.type)

        binding.eventTypeIcon.setImageResource(iconResId)
        binding.eventTitle.text = event.title
        binding.locationName.text = event.locationName
        binding.eventTime.text = event.time.getFormattedDate()

        binding.commentContent.text =
            if (comment.content.isNullOrBlank()) "(Image)" else comment.content
        binding.commentTime.text = comment.time.getLongTimeAgo()

        binding.deleteButton.setOnClickListener {
            onDeleteClick(populatedComment)
            Toast.makeText(binding.root.context, "Comment Deleted", Toast.LENGTH_SHORT).show()
        }
        binding.editButton.setOnClickListener { onEditClick(populatedComment) }
    }

    private fun bindTextComment(
        holder: BaseUserCommentViewHolder.TextCommentViewHolder,
        populatedComment: PopulatedComment,
    ) {
        with(holder.binding) {
            bindCommonFields(this, populatedComment)
            commentImageCard.visibility = View.GONE
        }
    }

    private fun bindImageComment(
        holder: BaseUserCommentViewHolder.ImageCommentViewHolder,
        populatedComment: PopulatedComment,
    ) {
        with(holder.binding) {
            bindCommonFields(this, populatedComment)

            if (!populatedComment.comment.imageUrl.isNullOrBlank()) {
                commentImageCard.visibility = View.VISIBLE
                ImageUtils.showImgInViewFromUrl(
                    populatedComment.comment.imageUrl,
                    commentImage,
                    commentImageLoader,
                )
            } else {
                commentImage.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = comments.size

    fun updateComments(newComments: List<PopulatedComment>) {
        Log.d(TAG, "updateComments: New List Size=${newComments.size}")
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }
}
