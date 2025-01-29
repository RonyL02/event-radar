package com.col.eventradar.utils

import android.content.Context
import android.util.TypedValue

object ThemeUtils {
    fun getThemeColor(context: Context): Int {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(android.R.attr.colorAccent, typedValue, true)
        return typedValue.data
    }
}
