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

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wirelessalien.android.bhagavadgita.activity.VerseDetailActivity
import com.wirelessalien.android.bhagavadgita.data.FavouriteVerse
import com.wirelessalien.android.bhagavadgita.databinding.FavVerseItemBinding

class FavouriteVerseAdapter(
    private val favoriteList: MutableList<FavouriteVerse>,
    private val onDeleteClicked: (FavouriteVerse) -> Unit,
    private val onAddNoteClicked: (FavouriteVerse) -> Unit
) : RecyclerView.Adapter<FavouriteVerseAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: FavVerseItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(favoriteItem: FavouriteVerse) {
            binding.combinedTitleTextView.text = favoriteItem.verseTitle
            binding.combinedContentTextView.text = favoriteItem.verseContent

            // Display user note if available
            if (!favoriteItem.userNote.isNullOrEmpty()) {
                binding.userNoteTextView.text = favoriteItem.userNote
                binding.userNoteTextView.visibility = View.VISIBLE
            } else {
                binding.userNoteTextView.visibility = View.GONE
            }

            binding.readAllBtn.setOnClickListener {
                val context = it.context
                val intent = Intent(context, VerseDetailActivity::class.java).apply {
                    putExtra("chapter_number", favoriteItem.chapterId)
                    putExtra("verse_title", favoriteItem.verseTitle)
                    putExtra("verse_text", favoriteItem.verseContent)
                    putExtra("verse_id", favoriteItem.verseId)
                }
                context.startActivity(intent)
            }

            binding.deleteBtn.setOnClickListener {
                onDeleteClicked(favoriteItem)
            }

            binding.addNoteBtn.setOnClickListener {
                onAddNoteClicked(favoriteItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FavVerseItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val favoriteItem = favoriteList[position]
        holder.bind(favoriteItem)
    }

    override fun getItemCount(): Int {
        return favoriteList.size
    }
}
