package com.col.eventradar.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.col.eventradar.databinding.ItemSearchResultBinding
import com.col.eventradar.ui.SearchFragment

class SearchResultsAdapter(private val onClick: (SearchFragment.SearchResult) -> Unit) :
    RecyclerView.Adapter<SearchResultsAdapter.SearchResultViewHolder>() {

    private var searchResults: List<SearchFragment.SearchResult> = emptyList()

    fun submitList(results: List<SearchFragment.SearchResult>) {
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

        fun bind(result: SearchFragment.SearchResult) {
            binding.resultName.text = result.name
            binding.root.setOnClickListener {
                onClick(result)
            }
        }
    }
}