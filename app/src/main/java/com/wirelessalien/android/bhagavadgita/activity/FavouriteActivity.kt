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

package com.wirelessalien.android.bhagavadgita.activity

import android.content.Context
// SharedPreferences and Gson are no longer needed for favorites
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog // For adding/editing notes
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wirelessalien.android.bhagavadgita.adapter.FavouriteVerseAdapter
import com.wirelessalien.android.bhagavadgita.data.FavoriteDbHelper
import com.wirelessalien.android.bhagavadgita.data.FavouriteVerse // This is the UI model
import com.wirelessalien.android.bhagavadgita.databinding.ActivityFavouriteBinding
import com.wirelessalien.android.bhagavadgita.databinding.DialogAddNoteBinding // For the note dialog
import com.wirelessalien.android.bhagavadgita.utils.Themes

class FavouriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavouriteBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavouriteVerseAdapter
    private val favoriteList = mutableListOf<FavouriteVerse>()
    private lateinit var dbHelper: FavoriteDbHelper
    private var currentTextSize: Int = 16 // Default text size

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Themes.loadTheme(this)

        binding = ActivityFavouriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.favoritesRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        dbHelper = FavoriteDbHelper(this)

        val sharedPrefTextSize = getSharedPreferences("text_size_prefs", Context.MODE_PRIVATE)
        currentTextSize = sharedPrefTextSize.getInt("text_size", 16) // Get the saved text size

        // Initialize and set the adapter
        // The list will be populated by loadFavoriteList
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


        // Set up a click listener for the expand/collapse - this can remain similar if FavouriteVerse retains isExpanded
        adapter.setOnExpandClickListener { position ->
            toggleItemExpansion(position)
        }
    }

    private fun loadFavoriteList() {
        val dbFavorites = dbHelper.getAllFavorites()
        favoriteList.clear()
        // Assuming FavouriteVerse now takes verseId, verseTitle (which might be part of verseText or need reconstruction),
        // verseContent, and userNote.
        // For simplicity, let's assume verse_text from DB is verseContent and verse_id is enough for title (e.g. "Verse 1.1")
        // You might need to fetch actual verse titles from another source if they are not in the favorites table.
        dbFavorites.forEach { entity ->
            favoriteList.add(
                FavouriteVerse(
                    chapterId = entity.chapterId, // Populate chapterId
                    verseId = entity.verseId,
                    verseTitle = "Verse ${entity.chapterId}.${entity.verseId}", // Updated placeholder for title
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
        val verseIdToDelete = verse.verseId
        val result = dbHelper.removeFavorite(verseIdToDelete)
        if (result > 0) {
            Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show()
            loadFavoriteList() // Reload the list
        } else {
            Toast.makeText(this, "Failed to remove favorite", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddNoteDialog(verse: FavouriteVerse) {
        val dialogBinding = DialogAddNoteBinding.inflate(layoutInflater)
        dialogBinding.noteEditText.setText(verse.userNote ?: "")

        AlertDialog.Builder(this)
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
