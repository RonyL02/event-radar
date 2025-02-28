package com.col.eventradar.ui.components

import android.util.Log
import androidx.fragment.app.Fragment
import android.widget.Toast

class ToastFragment : Fragment() {
    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        activity?.let {
            Toast.makeText(it, message, duration).show()
        } ?: Log.e(TAG, "ToastFragment is not attached!")
    }

    companion object {
        const val TAG = "ToastFragment"
    }
}