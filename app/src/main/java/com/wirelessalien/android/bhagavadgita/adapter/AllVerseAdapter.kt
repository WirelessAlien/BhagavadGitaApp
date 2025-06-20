
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
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wirelessalien.android.bhagavadgita.activity.VerseDetailActivity
import com.wirelessalien.android.bhagavadgita.data.Verse
import com.wirelessalien.android.bhagavadgita.databinding.AllVerseCardviewItemBinding

class AllVerseAdapter(
    private var verses: List<Verse>,
    private var textSize: Int,
    private var matchedText: String? = null,
    private var matchedContexts: List<SpannableString>? = null
) : RecyclerView.Adapter<AllVerseAdapter.AllVerseViewHolder>() {

    inner class AllVerseViewHolder(private val binding: AllVerseCardviewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(verse: Verse, matchedContext: SpannableString?) {
            binding.verseTitleTextView.text = verse.title
            binding.verseTextView.text = verse.text
            binding.matchedTextView.text = matchedContext // Set matched context here
            binding.verseTitleTextView.textSize = textSize.toFloat()
            binding.verseTextView.textSize = textSize.toFloat()

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
        val matchedContext = matchedContexts?.getOrNull(position)
        holder.bind(verses[position], matchedContext)
    }

    override fun getItemCount(): Int {
        return verses.size
    }

    fun updateTextSize(newSize: Int, filteredList: List<Verse>, matchedText: String?, matchedContexts: List<SpannableString>) {
        textSize = newSize
        this.matchedText = matchedText
        this.matchedContexts = matchedContexts
        verses = filteredList
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
            }
        }
    }
}

