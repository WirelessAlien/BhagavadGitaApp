/*
 * This file is part of BhagavadGitaApp <https://github.com/WirelessAlien/BhagavadGitaApp>
 * Copyright (C) 2023  WirelessAlien <https://github.com/WirelessAlien>
 *
 * BhagavadGitaApp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BhagavadGitaApp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.wirelessalien.android.bhagavadgita.adapter

// FavoriteVerseAdapter.kt
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wirelessalien.android.bhagavadgita.R
import com.wirelessalien.android.bhagavadgita.activity.VerseDetailActivity
import com.wirelessalien.android.bhagavadgita.data.FavouriteVerse
import com.wirelessalien.android.bhagavadgita.databinding.FavVerseItemBinding


class FavouriteVerseAdapter(private val favoriteList: MutableList<FavouriteVerse>) :
    RecyclerView.Adapter<FavouriteVerseAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: FavVerseItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(favoriteItem: FavouriteVerse) {
            binding.combinedTitleTextView.text = favoriteItem.verseTitle
            binding.combinedContentTextView.text = favoriteItem.verseContent
            binding.combinedTransliterationTextView.text = favoriteItem.transliteration
            binding.combinedWordMeaningTextView.text = favoriteItem.wordMeanings
            binding.combinedTranslation.text = favoriteItem.translationData
            binding.combinedCommentary.text = favoriteItem.commentaryData

            binding.combinedContentTextView.visibility = if (favoriteItem.isExpanded) View.VISIBLE else View.GONE
            binding.combinedTransliterationTextView.visibility = if (favoriteItem.isExpanded) View.VISIBLE else View.GONE
            binding.combinedWordMeaningTextView.visibility = if (favoriteItem.isExpanded) View.VISIBLE else View.GONE
            binding.combinedTranslation.visibility = if (favoriteItem.isExpanded) View.VISIBLE else View.GONE
            binding.combinedCommentary.visibility = if (favoriteItem.isExpanded) View.VISIBLE else View.GONE

            binding.combinedTransliterationTextViewH.visibility = if (favoriteItem.isExpanded) View.VISIBLE else View.GONE
            binding.combinedWordMeaningTextViewH.visibility = if (favoriteItem.isExpanded) View.VISIBLE else View.GONE
            binding.combinedTranslationH.visibility = if (favoriteItem.isExpanded) View.VISIBLE else View.GONE
            binding.combinedCommentaryH.visibility = if (favoriteItem.isExpanded) View.VISIBLE else View.GONE


           binding.readAllBtn.setOnClickListener {
               val context = it.context
               val intent = Intent(context, VerseDetailActivity::class.java).apply {
                   putExtra("chapter_number", favoriteItem.chapterId)
                   putExtra("verse_title", favoriteItem.verseTitle)
                   putExtra("verse_text", favoriteItem.verseContent)
                   putExtra("verse_transliteration", favoriteItem.transliteration)
                   putExtra("verse_word_meanings", favoriteItem.wordMeanings)

                   }
               context.startActivity(intent)
           }


            binding.deleteBtn.setOnClickListener {
                onDeleteClickListener?.let { click ->
                    click(bindingAdapterPosition)
                }
            }

            if (favoriteItem.isExpanded) {
                binding.toggleExpandBtn.check(R.id.expandBtn)
            } else {
                binding.toggleExpandBtn.check(R.id.collapseBtn)
            }

            // Set up a click listener for expanding/collapsing this item
            binding.expandBtn.setOnClickListener {
                favoriteItem.isExpanded = true
                notifyItemChanged(bindingAdapterPosition)
            }

            binding.collapseBtn.setOnClickListener {
                favoriteItem.isExpanded = false
                notifyItemChanged(bindingAdapterPosition)
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

    private var onDeleteClickListener: ((Int) -> Unit)? = null

    fun setOnDeleteClickListener(listener: (Int) -> Unit) {
        onDeleteClickListener = listener
    }

    private var onExpandClickListener: ((Int) -> Unit)? = null

    fun setOnExpandClickListener(listener: (Int) -> Unit) {
        onExpandClickListener = listener
    }
}
