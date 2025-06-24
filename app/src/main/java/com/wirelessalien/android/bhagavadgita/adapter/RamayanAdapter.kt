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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wirelessalien.android.bhagavadgita.R
import com.wirelessalien.android.bhagavadgita.data.RamayanVerse

class RamayanAdapter(private var verses: List<RamayanVerse>, private var textSize: Int) :
    RecyclerView.Adapter<RamayanAdapter.VerseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerseViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ramayan_verse, parent, false)
        return VerseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: VerseViewHolder, position: Int) {
        val currentVerse = verses[position]
        holder.bind(currentVerse)
    }

    override fun getItemCount() = verses.size

    fun updateData(newVerses: List<RamayanVerse>) {
        verses = newVerses
        notifyDataSetChanged()
    }

    inner class VerseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewKanda: TextView = itemView.findViewById(R.id.textViewKanda)
        private val textViewShlokaText: TextView = itemView.findViewById(R.id.textViewShlokaText)
        private val textViewTranslation: TextView = itemView.findViewById(R.id.textViewTranslation)
        private val textViewExplanation: TextView = itemView.findViewById(R.id.textViewExplanation)

        fun bind(verse: RamayanVerse) {
            // Kanda Title (Kanda, Sarga, Shloka number)
            if (verse.showKanda) {
                val kandaTitle = "${verse.kanda} - Sarga ${verse.sarga}, Shloka ${verse.shloka}"
                textViewKanda.text = kandaTitle
                textViewKanda.visibility = View.VISIBLE
            } else {
                textViewKanda.visibility = View.GONE
            }

            // Shloka Text
            if (verse.showShlokaText) {
                textViewShlokaText.text = verse.shlokaText
                textViewShlokaText.visibility = View.VISIBLE
            } else {
                textViewShlokaText.visibility = View.GONE
            }

            // Translation
            if (verse.showTranslation) {
                textViewTranslation.text = verse.translation ?: itemView.context.getString(R.string.no_translation_available)
                textViewTranslation.visibility = View.VISIBLE
            } else {
                textViewTranslation.visibility = View.GONE
            }

            // Explanation
            if (verse.showExplanation && !verse.explanation.isNullOrEmpty()) {
                textViewExplanation.text = verse.explanation
                textViewExplanation.visibility = View.VISIBLE
            } else {
                textViewExplanation.visibility = View.GONE
            }

            textViewKanda.textSize = textSize.toFloat()
            textViewShlokaText.textSize = textSize.toFloat()
            textViewTranslation.textSize = textSize.toFloat()
            textViewExplanation.textSize = textSize.toFloat()
        }
    }
}
