
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
import com.wirelessalien.android.bhagavadgita.data.Commentary

class CommentaryAdapter(private val commentary: List<Commentary>, private var textSize: Int) :
    RecyclerView.Adapter<CommentaryAdapter.CommentaryViewHolder>() {

    inner class CommentaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val authorNameTextView: TextView = itemView.findViewById(R.id.authorNameTextView)
        val commentaryTextView: TextView = itemView.findViewById(R.id.tversedescriptionTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentaryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.commentary_cardview_item, parent, false)
        return CommentaryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CommentaryViewHolder, position: Int) {
        val commentaryItem = commentary[position]

        // Bind data to the views
        holder.authorNameTextView.text = commentaryItem.authorName
        holder.commentaryTextView.text = commentaryItem.description
        holder.authorNameTextView.textSize = textSize.toFloat()
        holder.commentaryTextView.textSize = textSize.toFloat()
    }

    override fun getItemCount(): Int {
        return commentary.size
    }

    fun updateTextSize(newSize: Int) {
        textSize = newSize
        notifyDataSetChanged()
    }

    fun getAllCommentaryText(): String {
        var allCommentaryText = ""
        for (commentaryItem in commentary) {
            allCommentaryText += commentaryItem.description + "\n\n"
        }
        return allCommentaryText
    }
}

