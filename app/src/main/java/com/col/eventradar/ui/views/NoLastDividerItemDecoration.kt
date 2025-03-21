package com.col.eventradar.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class NoLastDividerItemDecoration(
    context: Context,
    private val orientation: Int,
    drawableRes: Int,
) : RecyclerView.ItemDecoration() {
    private val divider: Drawable? = ContextCompat.getDrawable(context, drawableRes)

    override fun onDraw(
        canvas: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        divider ?: return
        val childCount = parent.childCount
        val itemCount = parent.adapter?.itemCount ?: 0

        for (i in 0 until childCount) {
            val view = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(view)

            if (position == RecyclerView.NO_POSITION || position == itemCount - 1) continue

            val params = view.layoutParams as RecyclerView.LayoutParams

            if (orientation == RecyclerView.VERTICAL) {
                val left = parent.paddingLeft
                val right = parent.width - parent.paddingRight
                val top = view.bottom + params.bottomMargin
                val bottom = top + divider.intrinsicHeight
                divider.setBounds(left, top, right, bottom)
            } else {
                val top = parent.paddingTop
                val bottom = parent.height - parent.paddingBottom
                val left = view.right + params.rightMargin
                val right = left + divider.intrinsicWidth
                divider.setBounds(left, top, right, bottom)
            }

            divider.draw(canvas)
        }
    }
}
