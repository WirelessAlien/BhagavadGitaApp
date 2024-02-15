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

package com.wirelessalien.android.bhagavadgita.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wirelessalien.android.bhagavadgita.R
import com.wirelessalien.android.bhagavadgita.adapter.FavouriteVerseAdapter
import com.wirelessalien.android.bhagavadgita.data.FavouriteVerse
import com.wirelessalien.android.bhagavadgita.databinding.FragmentFavouriteBinding
import com.wirelessalien.android.bhagavadgita.utils.Themes

class FavouriteFragment : Fragment() {

    private lateinit var binding: FragmentFavouriteBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavouriteVerseAdapter
    private val favoriteList = mutableListOf<FavouriteVerse>()
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
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

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("favorites", Context.MODE_PRIVATE)

        val sharedPrefTextSize =
            requireActivity().getSharedPreferences("text_size_prefs", Context.MODE_PRIVATE)
        currentTextSize = sharedPrefTextSize.getInt("text_size", 16) // Get the saved text size

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

        if (favoriteList.isEmpty()) {
            // If the list is empty, show the emptyTextView
            binding.emptyTextView.visibility = View.VISIBLE
        } else {
            // If the list is not empty, hide the emptyTextView
            binding.emptyTextView.visibility = View.GONE
        }
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
