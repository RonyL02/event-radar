package com.col.eventradar.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.col.eventradar.api.locations.dto.LocationSearchResult
import com.col.eventradar.databinding.ItemSearchResultBinding

class LocationSearchResultsAdapter(
    private val onClick: (LocationSearchResult) -> Unit,
    private val onRemove: (LocationSearchResult) -> Unit,
) : RecyclerView.Adapter<LocationSearchResultsAdapter.SearchResultViewHolder>() {
    private var searchResults: List<LocationSearchResult> = emptyList()
    private var countries: List<Long> = emptyList()

    fun submitList(results: List<LocationSearchResult>) {
        searchResults = results
        notifyDataSetChanged()
    }

    fun updateCountries(newCountries: List<Long>) {
        countries = newCountries
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
        val result = searchResults[position]
        holder.bind(result)
    }

    override fun getItemCount(): Int = searchResults.size

    inner class SearchResultViewHolder(
        private val binding: ItemSearchResultBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(result: LocationSearchResult) {
            binding.apply {
                Log.d("SearchResultViewHolder", "bind: $result")
                val isExist = countries.contains(result.placeId)
                resultName.text = result.name
                resultCountry.text = result.country
                if (!isExist) {
                    root.setOnClickListener {
                        onClick(result)
                    }
                }

                addButton.isVisible = !isExist
                removeButton.isVisible = isExist

                removeButton.setOnClickListener {
                    Log.d("SearchResultViewHolder", "removedClick: $result")
                    onRemove(result)
                }
            }
        }
    }
}
