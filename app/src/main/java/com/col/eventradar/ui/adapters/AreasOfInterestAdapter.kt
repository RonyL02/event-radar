package com.col.eventradar.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.col.eventradar.databinding.ItemSearchResultBinding
import com.col.eventradar.models.AreaEntity

class AreasOfInterestAdapter(
    private val onRemove: (AreaEntity) -> Unit,
) : RecyclerView.Adapter<AreasOfInterestAdapter.SearchResultViewHolder>() {
    private var areas: List<AreaEntity> = emptyList()

    fun submitList(results: List<AreaEntity>) {
        areas = results
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SearchResultViewHolder {
        val binding =
            ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchResultViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: SearchResultViewHolder,
        position: Int,
    ) {
        val result = areas[position]
        holder.bind(result)
    }

    override fun getItemCount(): Int = areas.size

    inner class SearchResultViewHolder(
        private val binding: ItemSearchResultBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(area: AreaEntity) {
            binding.apply {
                resultName.text = area.name
                resultCountry.text = area.country
                addButton.isVisible = false
                removeButton.isVisible = true

                removeButton.setOnClickListener {
                    onRemove(area)
                }
            }
        }
    }
}
