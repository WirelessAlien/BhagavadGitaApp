package com.wirelessalien.android.bhagavadgita.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wirelessalien.android.bhagavadgita.R
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
        }
    }
}
