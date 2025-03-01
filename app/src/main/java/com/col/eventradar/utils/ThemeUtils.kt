package com.col.eventradar.utils

import android.content.Context
import android.util.TypedValue
import com.col.eventradar.R

object ThemeUtils {
    fun getThemeColor(context: Context): Int {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(R.attr.colorSecondary, typedValue, true)
        return typedValue.data
    }
}
