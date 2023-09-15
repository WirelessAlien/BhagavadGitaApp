
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

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wirelessalien.android.bhagavadgita.R
import com.wirelessalien.android.bhagavadgita.data.Commentary

class CommentaryAdapter(private val commentary: List<Commentary>) :
    RecyclerView.Adapter<CommentaryAdapter.CommentaryViewHolder>() {

    private var authorNameTextSize: Float = 16F // Default text size for author names in SP
    private var commentaryTextSize: Float = 16F // Default text size for commentaries in SP

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

        // Set the text sizes based on their respective variables
        holder.authorNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, authorNameTextSize.toFloat())
        holder.commentaryTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, commentaryTextSize.toFloat())

        // Bind data to the views
        holder.authorNameTextView.text = commentaryItem.authorName
        holder.commentaryTextView.text = commentaryItem.description
    }

    override fun getItemCount(): Int {
        return commentary.size
    }

    // Methods to set the text sizes dynamically
    fun setAuthorNameTextSize(newTextSize: Float) {
        this.authorNameTextSize = newTextSize
        notifyDataSetChanged()
    }

    fun setCommentaryTextSize(newTextSize: Float) {
        this.commentaryTextSize = newTextSize
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

