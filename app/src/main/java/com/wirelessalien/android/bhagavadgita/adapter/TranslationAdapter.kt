
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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wirelessalien.android.bhagavadgita.R
import com.wirelessalien.android.bhagavadgita.data.Translation

class TranslationAdapter(private val translations: List<Translation>, private var textSize: Int) :
    RecyclerView.Adapter<TranslationAdapter.TranslationViewHolder>() {

    inner class TranslationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val authorNameTextView: TextView = itemView.findViewById(R.id.authorNameTextView)
        val translationTextView: TextView = itemView.findViewById(R.id.tversedescriptionTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranslationViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.translation_cardview_item, parent, false)
        return TranslationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TranslationViewHolder, position: Int) {
        val translation = translations[position]

        holder.authorNameTextView.text = translation.authorName
        holder.translationTextView.text = translation.description
        holder.translationTextView.textSize = textSize.toFloat()
    }


    override fun getItemCount(): Int {
        return translations.size
    }

    fun updateTextSize(newSize: Int) {
        textSize = newSize
        notifyDataSetChanged()
    }

    fun getAllTranslationText(): String {
        var allTranslationText = ""
        for (translation in translations) {
            allTranslationText += translation.description + "\n\n"
        }
        return allTranslationText
    }
}
