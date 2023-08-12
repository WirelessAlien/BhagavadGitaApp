
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
import com.wirelessalien.android.bhagavadgita.activity.VerseTranslationActivity
import com.wirelessalien.android.bhagavadgita.data.Translation
import com.wirelessalien.android.bhagavadgita.databinding.TranslationCardviewItemBinding

class TranslationAdapter(private val Translation: List<Translation>, private val isClickActionEnabled: Boolean = false)  :
    RecyclerView.Adapter<TranslationAdapter.TranslationViewHolder>() {

    inner class TranslationViewHolder(private val binding: TranslationCardviewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(translation: Translation) {
            binding.authorNameTextView.text = translation.authorName
            binding.tversedescriptionTextView.text = translation.description

            if (isClickActionEnabled) {
                binding.root.setOnClickListener {
                    val intent = Intent(binding.root.context, VerseTranslationActivity::class.java)
                    intent.putExtra("verse_number", translation.verse_number)
                    intent.putExtra("description", translation.description)
                    binding.root.context.startActivity(intent)
                }
            } else {
                binding.root.setOnClickListener(null)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranslationViewHolder {
        val binding = TranslationCardviewItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TranslationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TranslationViewHolder, position: Int) {
        holder.bind(Translation[position])
    }

    override fun getItemCount(): Int {
        return Translation.size
    }
}
