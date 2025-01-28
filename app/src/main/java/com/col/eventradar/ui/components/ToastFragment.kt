package com.col.eventradar.ui.components

import androidx.fragment.app.Fragment
import android.widget.Toast

class ToastFragment : Fragment() {
    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        context?.let {
            Toast.makeText(it, message, duration).show()
        }
    }
}