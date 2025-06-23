/*
 *  This file is part of BhagavadGitaApp. @WirelessAlien
 *
 *  BhagavadGitaApp is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  BhagavadGitaApp is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with BhagavadGitaApp. If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.wirelessalien.android.bhagavadgita.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wirelessalien.android.bhagavadgita.data.Chapter
import com.wirelessalien.android.bhagavadgita.data.Verse
import com.wirelessalien.android.bhagavadgita.databinding.SearchResultItemBinding // Ensure this binding is generated

class SearchResultsAdapter(
    private var results: List<Any>, // Can be Chapter or Verse
    private val onItemClicked: (Any) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.SearchResultViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val binding = SearchResultItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SearchResultViewHolder(binding, onItemClicked)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(results[position])
    }

    override fun getItemCount(): Int = results.size

    fun updateData(newResults: List<Any>) {
        results = newResults
        notifyDataSetChanged()
    }

    class SearchResultViewHolder(
        private val binding: SearchResultItemBinding,
        private val onItemClicked: (Any) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Any) {
            when (item) {
                is Chapter -> {
                    binding.searchItemTitle.text = "Chapter ${item.chapter_number}: ${item.name}"
                    binding.searchItemDescription.text = item.name_translation ?: item.chapter_summary
                }
                is Verse -> {
                    binding.searchItemTitle.text = "Verse ${item.chapter_number}.${item.verse_number}"
                    binding.searchItemDescription.text = item.text ?: item.transliteration
                }
                else -> {
                    binding.searchItemTitle.text = "Unknown Item"
                    binding.searchItemDescription.text = ""
                }
            }
            binding.root.setOnClickListener { onItemClicked(item) }
        }
    }
}
