
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
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wirelessalien.android.bhagavadgita.adapter.TranslationAdapter
import com.wirelessalien.android.bhagavadgita.data.Translation
import com.wirelessalien.android.bhagavadgita.databinding.ActivityVerseTranslationBinding
import com.wirelessalien.android.bhagavadgita.utils.Themes
import java.io.IOException


class VerseTranslationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerseTranslationBinding
    private var currentTextSize: Int = 16

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Themes.loadTheme(this)

        binding = ActivityVerseTranslationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPrefTextSize = getSharedPreferences("text_size_prefs", Context.MODE_PRIVATE)
        currentTextSize = sharedPrefTextSize.getInt("text_size", 16) // Get the saved text size

        // Retrieve the selected verse number from the intent
        val verseNumber = intent.getIntExtra("verse_id", 0)

        // Find the translations for the given verseNumber
        val translations = getTranslationsForVerse(verseNumber)

        // Set up the RecyclerView to display the translations in CardViews
        val adapter = TranslationAdapter(translations, 16)
        binding.translationRecyclerView.adapter = adapter
        binding.translationRecyclerView.layoutManager = LinearLayoutManager(this)

        updateAdapterTextSize(currentTextSize)
    }

    private fun getTranslationsForVerse(verseNumber: Int): List<Translation> {
        // Retrieve the list of translations from the JSON file
        val jsonString = getJsonDataFromAsset("translation.json")
        Log.d("VerseTranslationActivity","verseNumber $verseNumber -> $jsonString")
        val gson = Gson()
        val listTranslationType = object : TypeToken<List<Translation>>() {}.type
        val translations: List<Translation> = gson.fromJson(jsonString, listTranslationType)

        // Filter the list of translations to get the translations for the given verse number
        return translations.filter { it.verse_id == verseNumber }
    }

    private fun getJsonDataFromAsset(fileName: String): String? {
        return try {
            applicationContext.assets.open(fileName).bufferedReader().use {
                it.readText()
            }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            null
        }
    }

    private fun updateAdapterTextSize(newSize: Int) {
        val recyclerViewT = binding.translationRecyclerView
        val adapterT = recyclerViewT.adapter as? TranslationAdapter
        adapterT?.updateTextSize(newSize)

    }
}
