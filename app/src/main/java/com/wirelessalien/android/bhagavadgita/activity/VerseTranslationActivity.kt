package com.wirelessalien.android.bhagavadgita.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wirelessalien.android.bhagavadgita.R
import com.wirelessalien.android.bhagavadgita.adapter.TranslationAdapter
import com.wirelessalien.android.bhagavadgita.data.Translation
import java.io.IOException

class VerseTranslationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verse_translation)

        // Retrieve the selected verse number from the intent
        val verseNumber = intent.getIntExtra("verse_number", 0)


        // Find the translations for the given verseNumber
        val translations = getTranslationsForVerse(verseNumber)

        // Set up the RecyclerView to display the translations in CardViews
        val recyclerView = findViewById<RecyclerView>(R.id.translationRecyclerView)
        val adapter = TranslationAdapter(translations)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun getTranslationsForVerse (verseNumber: Int): List<Translation> {
        // Retrieve the list of translations from the JSON file
        val jsonString = getJsonDataFromAsset("translation.json")
        val gson = Gson()
        val listTranslationType = object : TypeToken<List<Translation>>() {}.type
        val translations: List<Translation> = gson.fromJson(jsonString, listTranslationType)

        // Filter the list of translations to get the translations for the given verse number
        return translations.filter { it.verse_number == verseNumber }
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
}