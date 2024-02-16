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

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wirelessalien.android.bhagavadgita.R
import com.wirelessalien.android.bhagavadgita.activity.VerseDetailActivity
import com.wirelessalien.android.bhagavadgita.data.Verse

class SliderVerseAdapter(private val verses: List<Verse>) :
    RecyclerView.Adapter<SliderVerseAdapter.SliderVerseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderVerseViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate( R.layout.slider_verse_cardview, parent, false)
        return SliderVerseViewHolder(view)
    }

    override fun onBindViewHolder(holder: SliderVerseViewHolder, position: Int) {
        val verse = verses[position]
        holder.bind(verse)
    }

    override fun getItemCount(): Int = verses.size

    inner class SliderVerseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val verseTextView: TextView = itemView.findViewById(R.id.verseTextView)

        fun bind(verse: Verse) {
            verseTextView.text = verse.text

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, VerseDetailActivity::class.java).apply {
                    putExtra("chapter_number", verse.chapter_number)
                    putExtra("verse_title", verse.title)
                    putExtra("verse_text", verse.text)
                    putExtra("verse_transliteration", verse.transliteration)
                    putExtra("verse_word_meanings", verse.word_meanings)
                    // Add other verse details here if needed
                }
                itemView.context.startActivity(intent)
            }
        }
    }
}
