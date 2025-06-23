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
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wirelessalien.android.bhagavadgita.R
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

        val sharedPrefTextSize = PreferenceManager.getDefaultSharedPreferences(this)
        currentTextSize = sharedPrefTextSize.getInt("text_size_preference", 16) // Get the saved text size

        adapter = FavouriteVerseAdapter(favoriteList,
            onDeleteClicked = { verse ->
                deleteFavorite(verse)
            },
            onAddNoteClicked = { verse ->
                showAddNoteDialog(verse)
            }
        )
        recyclerView.adapter = adapter

        loadFavoriteList()

    }

    private fun loadFavoriteList() {
        val dbFavorites = dbHelper.getAllFavorites()
        favoriteList.clear()
        dbFavorites.forEach { entity ->
            favoriteList.add(
                FavouriteVerse(
                    chapterId = entity.chapterId,
                    verseId = entity.verseId,
                    verseTitle = entity.verseTitle,
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
            Toast.makeText(this, getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show()
            loadFavoriteList()
        } else {
            Toast.makeText(this, getString(R.string.failed_to_remove_from_favorites), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddNoteDialog(verse: FavouriteVerse) {
        val dialogBinding = DialogAddNoteBinding.inflate(layoutInflater)
        dialogBinding.noteEditText.setText(verse.userNote ?: "")

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.add_edit_note_for, verse.verseTitle))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.save)) { dialog, _ ->
                val noteText = dialogBinding.noteEditText.text.toString()
                dbHelper.addOrUpdateUserNote(verse.verseId, noteText)
                loadFavoriteList()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }
}
