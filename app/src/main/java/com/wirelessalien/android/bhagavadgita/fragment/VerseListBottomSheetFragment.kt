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

package com.wirelessalien.android.bhagavadgita.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wirelessalien.android.bhagavadgita.R
import com.wirelessalien.android.bhagavadgita.data.Verse

class VerseListBottomSheetFragment(private val verses: List<Verse>, private val onVerseSelected: (Verse) -> Unit) : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.verse_list_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewvlbs)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = VerseListAdapter(verses, onVerseSelected)
    }

    class VerseListAdapter(private val verses: List<Verse>, private val onVerseSelected: (Verse) -> Unit) : RecyclerView.Adapter<VerseListAdapter.ViewHolder>() {

        class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            val titleTextView: TextView = view.findViewById(R.id.verseTitleTextView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.verse_cardview_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val verse = verses[position]
            holder.titleTextView.text = verse.title
            holder.view.setOnClickListener {
                onVerseSelected(verse)
            }
        }

        override fun getItemCount() = verses.size
    }
}