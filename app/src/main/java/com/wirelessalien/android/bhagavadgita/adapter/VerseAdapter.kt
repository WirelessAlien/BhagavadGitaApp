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

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.wirelessalien.android.bhagavadgita.R
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

            val sharedPreferences = binding.root.context.getSharedPreferences("read_verses", Context.MODE_PRIVATE)
            val verseId = verse.verse_id
            val isVerseRead = sharedPreferences.getBoolean("$verseId", false)

           if (isVerseRead) {
               binding.cardviewVerseItem.strokeColor = ContextCompat.getColor(binding.root.context, R.color.md_theme_light_primary)
           } else {
               //do nothing
           }

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

    fun updateProgressData() {
        notifyDataSetChanged()
    }
}
