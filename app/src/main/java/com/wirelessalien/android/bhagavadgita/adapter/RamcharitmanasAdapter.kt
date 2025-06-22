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
import com.wirelessalien.android.bhagavadgita.data.RamcharitmanasVerse

class RamcharitmanasAdapter(private var verses: List<RamcharitmanasVerse>) :
    RecyclerView.Adapter<RamcharitmanasAdapter.VerseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerseViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ramcharitmanas_verse, parent, false)
        return VerseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: VerseViewHolder, position: Int) {
        val currentVerse = verses[position]
        holder.bind(currentVerse)
    }

    override fun getItemCount() = verses.size

    fun updateData(newVerses: List<RamcharitmanasVerse>) {
        verses = newVerses
        notifyDataSetChanged()
    }

    inner class VerseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewKanda: TextView = itemView.findViewById(R.id.textViewKanda)
        private val textViewShlokaText: TextView = itemView.findViewById(R.id.textViewShlokaText)
        private val textViewTranslation: TextView = itemView.findViewById(R.id.textViewTranslation)
        private val textViewExplanation: TextView = itemView.findViewById(R.id.textViewExplanation)

        fun bind(verse: RamcharitmanasVerse) {
            val kandaTitle = "${verse.kanda} - Sarga ${verse.sarga}, Shloka ${verse.shloka}"
            textViewKanda.text = kandaTitle
            textViewShlokaText.text = verse.shlokaText
            textViewTranslation.text = verse.translation ?: "No translation available."

            if (verse.explanation.isNullOrEmpty()) {
                textViewExplanation.visibility = View.GONE
            } else {
                textViewExplanation.text = verse.explanation
                textViewExplanation.visibility = View.VISIBLE // Default to visible if explanation exists
            }

            // Optional: Add an OnClickListener to toggle explanation visibility or navigate to a detail view
            // itemView.setOnClickListener {
            //     textViewExplanation.visibility = if (textViewExplanation.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            // }
        }
    }
}
