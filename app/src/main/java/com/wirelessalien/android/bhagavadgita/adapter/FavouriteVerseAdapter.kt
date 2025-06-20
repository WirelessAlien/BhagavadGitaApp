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

            // Hide fields no longer in FavouriteVerse model
            binding.combinedTransliterationTextView.visibility = View.GONE
            binding.combinedWordMeaningTextView.visibility = View.GONE
            binding.combinedTranslation.visibility = View.GONE
            binding.combinedCommentary.visibility = View.GONE
            binding.combinedTransliterationTextViewH.visibility = View.GONE
            binding.combinedWordMeaningTextViewH.visibility = View.GONE
            binding.combinedTranslationH.visibility = View.GONE
            binding.combinedCommentaryH.visibility = View.GONE

            // Display user note if available
            if (!favoriteItem.userNote.isNullOrEmpty()) {
                binding.userNoteTextView.text = favoriteItem.userNote
                binding.userNoteTextView.visibility = View.VISIBLE
            } else {
                binding.userNoteTextView.visibility = View.GONE
            }

            // Content and Note visibility based on expansion
            binding.combinedContentTextView.visibility = if (favoriteItem.isExpanded) View.VISIBLE else View.GONE
            binding.userNoteTextView.visibility = if (favoriteItem.isExpanded && !favoriteItem.userNote.isNullOrEmpty()) View.VISIBLE else View.GONE


            binding.readAllBtn.setOnClickListener {
                val context = it.context
                val intent = Intent(context, VerseDetailActivity::class.java).apply {
                    putExtra("chapter_number", favoriteItem.chapterId) // Use the correct chapterId
                    putExtra("verse_title", favoriteItem.verseTitle)
                    putExtra("verse_text", favoriteItem.verseContent)
                    // Pass global verse_id as an extra field, if VerseDetailActivity needs it to pinpoint the exact verse
                    // For instance, if chapter_number and verse_number (within chapter) are primary keys for lookup.
                    // The current VerseDetailActivity seems to find the verse by title within the chapter.
                    // If favoriteItem.verseId is the global ID, ensure VerseDetailActivity can use it.
                    // Let's assume favoriteItem.verseTitle is unique enough within the chapter for now.
                }
                context.startActivity(intent)
            }

            binding.deleteBtn.setOnClickListener {
                onDeleteClicked(favoriteItem)
            }

            binding.addNoteBtn.setOnClickListener { // Assuming you add an addNoteBtn in your layout
                onAddNoteClicked(favoriteItem)
            }

            if (favoriteItem.isExpanded) {
                binding.toggleExpandBtn.check(R.id.expandBtn)
            } else {
                binding.toggleExpandBtn.check(R.id.collapseBtn)
            }

            binding.expandBtn.setOnClickListener {
                onExpandClickListener?.let { click ->
                    click(bindingAdapterPosition)
                }
            }

            binding.collapseBtn.setOnClickListener {
                onExpandClickListener?.let { click ->
                    click(bindingAdapterPosition)
                }
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

    // Keep setOnExpandClickListener as it's used by the Activity/Fragment
    private var onExpandClickListener: ((Int) -> Unit)? = null
    fun setOnExpandClickListener(listener: (Int) -> Unit) {
        onExpandClickListener = listener
    }
}
