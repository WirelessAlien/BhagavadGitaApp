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
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wirelessalien.android.bhagavadgita.adapter.FavouriteVerseAdapter
import com.wirelessalien.android.bhagavadgita.data.FavouriteVerse
import com.wirelessalien.android.bhagavadgita.databinding.ActivityFavouriteBinding

class FavouriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavouriteBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavouriteVerseAdapter
    private val favoriteList = mutableListOf<FavouriteVerse>()
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavouriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.favoritesRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("favorites", Context.MODE_PRIVATE)

        // Load the list of favorite items
        loadFavoriteList()

        // Initialize and set the adapter
        adapter = FavouriteVerseAdapter(favoriteList)
        recyclerView.adapter = adapter

        // Set up a click listener for the delete button in the adapter
        adapter.setOnDeleteClickListener { position ->
            deleteFavorite(position)
        }

        adapter.setOnExpandClickListener { position ->
            toggleItemExpansion(position)
        }
    }

    private fun loadFavoriteList() {
        val favoritesJson = sharedPreferences.getString("favoriteList", "[]")
        val favoriteListType = object : TypeToken<List<FavouriteVerse>>() {}.type
        favoriteList.clear()
        favoriteList.addAll(gson.fromJson(favoritesJson, favoriteListType))
    }

    private fun saveFavoriteList() {
        val favoritesJson = gson.toJson(favoriteList)
        sharedPreferences.edit().putString("favoriteList", favoritesJson).apply()
    }

    private fun deleteFavorite(position: Int) {
        if (position in 0 until favoriteList.size) {
            favoriteList.removeAt(position)
            adapter.notifyItemRemoved(position)
            saveFavoriteList()
        }
    }

    private fun toggleItemExpansion(position: Int) {
        if (position in 0 until favoriteList.size) {
            val item = favoriteList[position]
            item.isExpanded = !item.isExpanded
            adapter.notifyItemChanged(position)
        }
    }
}
