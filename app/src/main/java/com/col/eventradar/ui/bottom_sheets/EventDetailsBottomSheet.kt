package com.col.eventradar.ui.bottom_sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.col.eventradar.databinding.FragmentEventDetailsBottmSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class EventDetailsBottomSheet : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentEventDetailsBottmSheetBinding.inflate(inflater, container, false).root

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}