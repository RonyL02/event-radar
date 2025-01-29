package com.col.eventradar.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.col.eventradar.databinding.ItemSearchResultBinding
import com.col.eventradar.models.LocationSearchResult

class LocationSearchResultsAdapter(private val onClick: (LocationSearchResult) -> Unit) :
    RecyclerView.Adapter<LocationSearchResultsAdapter.SearchResultViewHolder>() {

    private var searchResults: List<LocationSearchResult> = emptyList()

    fun submitList(results: List<LocationSearchResult>) {
        searchResults = results
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val binding =
            ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        val result = searchResults[position]
        holder.bind(result)
    }

    override fun getItemCount(): Int = searchResults.size

    inner class SearchResultViewHolder(private val binding: ItemSearchResultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(result: LocationSearchResult) {
            binding.resultName.text = result.locationName
            binding.root.setOnClickListener {
                onClick(result)
            }
        }
    }
}