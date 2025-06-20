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

import android.content.Context
// SharedPreferences and Gson are no longer needed for favorites
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog // For adding/editing notes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.wirelessalien.android.bhagavadgita.R
import com.wirelessalien.android.bhagavadgita.adapter.FavouriteVerseAdapter
import com.wirelessalien.android.bhagavadgita.data.FavoriteDbHelper
import com.wirelessalien.android.bhagavadgita.data.FavouriteVerse // UI Model
import com.wirelessalien.android.bhagavadgita.databinding.DialogAddNoteBinding // For the note dialog
import com.wirelessalien.android.bhagavadgita.databinding.FragmentFavouriteBinding
import com.wirelessalien.android.bhagavadgita.utils.Themes
import android.widget.Toast // Import Toast


class FavouriteFragment : Fragment() {

    private lateinit var binding: FragmentFavouriteBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavouriteVerseAdapter
    private val favoriteList = mutableListOf<FavouriteVerse>()
    private lateinit var dbHelper: FavoriteDbHelper
    private var currentTextSize: Int = 16 // Default text size

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavouriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        exitTransition = inflater.inflateTransition(R.transition.slide_right)
        Themes.loadTheme(requireActivity())

        recyclerView = binding.favoritesRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        dbHelper = FavoriteDbHelper(requireContext())

        val sharedPrefTextSize =
            requireActivity().getSharedPreferences("text_size_prefs", Context.MODE_PRIVATE)
        currentTextSize = sharedPrefTextSize.getInt("text_size", 16) // Get the saved text size

        // Initialize and set the adapter
        adapter = FavouriteVerseAdapter(favoriteList,
            onDeleteClicked = { verse ->
                deleteFavorite(verse)
            },
            onAddNoteClicked = { verse ->
                showAddNoteDialog(verse)
            }
        )
        recyclerView.adapter = adapter

        // Load the list of favorite items
        loadFavoriteList()

        // Set up a click listener for the expand/collapse
        adapter.setOnExpandClickListener { position ->
            toggleItemExpansion(position)
        }
    }

    private fun loadFavoriteList() {
        val dbFavorites = dbHelper.getAllFavorites()
        favoriteList.clear()
        dbFavorites.forEach { entity ->
            favoriteList.add(
                FavouriteVerse(
                    chapterId = entity.chapterId, // Populate chapterId
                    verseId = entity.verseId,
                    verseTitle = "Verse ${entity.chapterId}.${entity.verseId}", // Updated placeholder
                    verseContent = entity.verseText,
                    userNote = entity.userNote
                )
            )
        }
        adapter.notifyDataSetChanged()

        if (favoriteList.isEmpty()) {
            binding.emptyTextView.visibility = View.VISIBLE
        } else {
            binding.emptyTextView.visibility = View.GONE
        }
    }

    private fun deleteFavorite(verse: FavouriteVerse) {
        val result = dbHelper.removeFavorite(verse.verseId)
        if (result > 0) {
            Toast.makeText(requireContext(), "Removed from Favorites", Toast.LENGTH_SHORT).show()
            loadFavoriteList() // Reload the list
        } else {
            Toast.makeText(requireContext(), "Failed to remove favorite", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddNoteDialog(verse: FavouriteVerse) {
        val dialogBinding = DialogAddNoteBinding.inflate(layoutInflater)
        dialogBinding.noteEditText.setText(verse.userNote ?: "")

        AlertDialog.Builder(requireContext())
            .setTitle("Add/Edit Note for ${verse.verseTitle}")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { dialog, _ ->
                val noteText = dialogBinding.noteEditText.text.toString()
                dbHelper.addOrUpdateUserNote(verse.verseId, noteText)
                loadFavoriteList() // Refresh list to show new note
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun toggleItemExpansion(position: Int) {
        if (position in 0 until favoriteList.size) {
            val item = favoriteList[position]
            item.isExpanded = !item.isExpanded
            adapter.notifyItemChanged(position)
        }
    }
}
