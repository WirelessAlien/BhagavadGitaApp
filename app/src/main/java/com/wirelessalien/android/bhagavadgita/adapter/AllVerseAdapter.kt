
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

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wirelessalien.android.bhagavadgita.activity.VerseDetailActivity
import com.wirelessalien.android.bhagavadgita.data.Translation
import com.wirelessalien.android.bhagavadgita.data.Verse
import com.wirelessalien.android.bhagavadgita.databinding.AllVerseCardviewItemBinding

class AllVerseAdapter(
    private var verses: List<Verse>,
    private var textSize: Int,
    private var translations: Map<Int, Translation>,
) : RecyclerView.Adapter<AllVerseAdapter.AllVerseViewHolder>() {

    inner class AllVerseViewHolder(private val binding: AllVerseCardviewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(verse: Verse) {
            binding.verseTitleTextView.text = verse.title
            binding.verseTextView.text = verse.text
            binding.verseTitleTextView.textSize = textSize.toFloat()
            binding.verseTextView.textSize = textSize.toFloat()

            val translation = translations[verse.verse_id]
            translation?.let {
                binding.verseDescriptionTextView.text = it.description
            }

            binding.root.setOnClickListener {
                val intent = newIntent(binding.root.context, verse)
                binding.root.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllVerseViewHolder {
        val binding = AllVerseCardviewItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AllVerseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AllVerseViewHolder, position: Int) {
        holder.bind(verses[position])
    }

    override fun getItemCount(): Int {
        return verses.size
    }

    fun updateTextSize(newSize: Int, filteredList: List<Verse>? = null) {
        textSize = newSize
        filteredList?.let {
            verses = it
        }
        notifyDataSetChanged()
    }

    companion object {
        fun newIntent(context: Context, verse: Verse): Intent {
            return Intent(context, VerseDetailActivity::class.java).apply {
                putExtra("chapter_number", verse.chapter_number)
                putExtra("verse_title", verse.title)
                putExtra("verse_text", verse.text)
                putExtra("verse_transliteration", verse.transliteration)
                putExtra("verse_word_meanings", verse.word_meanings)
                // Add other verse details here if needed
            }
        }
    }
}
