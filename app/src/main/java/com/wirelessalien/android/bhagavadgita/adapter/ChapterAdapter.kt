
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
import com.wirelessalien.android.bhagavadgita.activity.ChapterDetailsActivity
import com.wirelessalien.android.bhagavadgita.data.Chapter
import com.wirelessalien.android.bhagavadgita.databinding.ChapterCardviewItemBinding


class ChapterAdapter(private val chapters: List<Chapter>, private var textSize: Int) :
    RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>() {

    inner class ChapterViewHolder(private val binding: ChapterCardviewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var currentPosition: Int = -1

        init {
            binding.root.setOnClickListener {
                if (currentPosition != RecyclerView.NO_POSITION) {
                    val chapter = chapters[currentPosition]
                    val intent = Intent(binding.root.context, ChapterDetailsActivity::class.java)
                    intent.apply {
                        putExtra("chapter_number", chapter.chapter_number)
                        putExtra("chapter_name", chapter.name)
                        putExtra("name_meaning", chapter.name_meaning)
                        putExtra("chapter_summary", chapter.chapter_summary)
                        putExtra("chapter_summary_hindi", chapter.chapter_summary_hindi)
                        putExtra("verses_count", chapter.verses_count)
                    }
                    binding.root.context.startActivity(intent)
                }
            }
        }

        fun bind(chapter: Chapter, position: Int) {
            currentPosition = position
            binding.chapterT.text = "Chapter"
            binding.chapterNumberTextView.text = chapter.chapter_number.toString()
            binding.chapterNameTextView.text = chapter.name
            binding.chapterNameMeaningTextView.text = chapter.name_meaning
            binding.verseCount.text = chapter.verses_count.toString()

            // Set the text size for TextViews
            binding.chapterT.textSize = textSize.toFloat()
            binding.chapterNumberTextView.textSize = textSize.toFloat()
            binding.chapterNameTextView.textSize = textSize.toFloat()
            binding.chapterNameMeaningTextView.textSize = textSize.toFloat()
            binding.verseCount.textSize = textSize.toFloat()

            val totalVerses = chapter.verses_count

            // Calculate the number of read verses
            val sharedPreferences = binding.root.context.getSharedPreferences("read_verses", Context.MODE_PRIVATE)
            val readVerses = sharedPreferences.all.keys.count {
                it.endsWith("-chapter") && sharedPreferences.getInt(it, 0) == chapter.chapter_number && sharedPreferences.getBoolean(it.removeSuffix("-chapter"), false)            }

            val progress = (readVerses.toDouble() / totalVerses.toDouble()) * 100

            binding.progressBarReadCount.progress = progress.toInt()
            binding.progressTextView.text = String.format("%.2f%%", progress)


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val binding = ChapterCardviewItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        holder.bind(chapters[position], position)
    }

    override fun getItemCount(): Int {
        return chapters.size
    }

    fun updateTextSize(newSize: Int) {
        textSize = newSize
        notifyDataSetChanged()
    }

    fun updateProgressData() {
        notifyDataSetChanged()
    }
}

