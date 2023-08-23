package com.wirelessalien.android.bhagavadgita.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wirelessalien.android.bhagavadgita.R
import com.wirelessalien.android.bhagavadgita.data.Commentary

class CommentaryAdapter(private val commentary: List<Commentary>) :
    RecyclerView.Adapter<CommentaryAdapter.CommentaryViewHolder>() {

    class CommentaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val authorNameTextView: TextView = itemView.findViewById(R.id.authorNameTextView)
        val translationTextView: TextView = itemView.findViewById(R.id.tversedescriptionTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentaryViewHolder {

        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.commentary_cardview_item, parent, false) // Replace with your layout XML
        return CommentaryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CommentaryViewHolder, position: Int) {
        val commentary = commentary[position]


        holder.authorNameTextView.text = commentary.authorName
        holder.translationTextView.text = commentary.description
    }

    override fun getItemCount(): Int {
        return commentary.size
    }
}
