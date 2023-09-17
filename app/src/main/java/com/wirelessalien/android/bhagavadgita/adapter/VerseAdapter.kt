
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
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wirelessalien.android.bhagavadgita.activity.VerseDetailActivity
import com.wirelessalien.android.bhagavadgita.data.Verse
import com.wirelessalien.android.bhagavadgita.databinding.VerseCardviewItemBinding

class VerseAdapter(private val verses: List<Verse>, private var textSize: Int) :
    RecyclerView.Adapter<VerseAdapter.VerseViewHolder>() {

    inner class VerseViewHolder(private val binding: VerseCardviewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(verse: Verse) {
            binding.verseTitleTextView.text = verse.title
            binding.verseTitleTextView.textSize = textSize.toFloat()

            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, VerseDetailActivity::class.java)
                intent.putExtra("chapter_number", verse.chapter_number)
                intent.putExtra("verse_title", verse.title)
                intent.putExtra("verse_text", verse.text)
                intent.putExtra("verse_transliteration", verse.transliteration)
                intent.putExtra("verse_word_meanings", verse.word_meanings)
                // Add other verse details here if needed
                binding.root.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerseViewHolder {
        val binding = VerseCardviewItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VerseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VerseViewHolder, position: Int) {
        holder.bind(verses[position])
    }

    override fun getItemCount(): Int {
        return verses.size
    }

    fun updateTextSize(newSize: Int) {
        textSize = newSize
        notifyDataSetChanged()
    }
}
