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
                    intent.putExtra("verse_number", translation.verseNumber)
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
