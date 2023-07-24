package com.wirelessalien.android.bhagavadgita.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wirelessalien.android.bhagavadgita.activity.ChapterDetailActivity
import com.wirelessalien.android.bhagavadgita.data.Chapter
import com.wirelessalien.android.bhagavadgita.databinding.ChapterCardviewItemBinding

class ChapterAdapter(private val chapters: List<Chapter>) :
    RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>() {

    inner class ChapterViewHolder(private val binding: ChapterCardviewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chapter: Chapter) {
            binding.chapterNumberTextView.text = chapter.chapter_number.toString()
            binding.chapterNameTextView.text = chapter.name
            binding.chapterNameMeaningTextView.text = chapter.name_meaning

            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, ChapterDetailActivity::class.java)
                intent.putExtra("chapter_number", chapter.chapter_number)
                intent.putExtra("chapter_name", chapter.name)
                intent.putExtra("name_meaning", chapter.name_meaning)
                intent.putExtra("chapter_summary", chapter.chapter_summary)
                intent.putExtra("chapter_summary_hindi", chapter.chapter_summary_hindi)
                // Add other chapter details here if needed
                binding.root.context.startActivity(intent)
            }
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
        holder.bind(chapters[position])
    }

    override fun getItemCount(): Int {
        return chapters.size
    }
}
