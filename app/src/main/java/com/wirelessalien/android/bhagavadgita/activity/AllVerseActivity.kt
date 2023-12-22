package com.wirelessalien.android.bhagavadgita.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wirelessalien.android.bhagavadgita.adapter.AllVerseAdapter
import com.wirelessalien.android.bhagavadgita.data.Commentary
import com.wirelessalien.android.bhagavadgita.data.Translation
import com.wirelessalien.android.bhagavadgita.data.Verse
import com.wirelessalien.android.bhagavadgita.databinding.AllVerseActivityBinding
import com.wirelessalien.android.bhagavadgita.utils.Themes
import kotlinx.coroutines.*
import java.io.IOException

class AllVerseActivity: AppCompatActivity() {

    private lateinit var binding: AllVerseActivityBinding
    private var verseList: List<Verse> = emptyList()
    private var currentTextSize: Int = 16
    private lateinit var searchView: SearchView


    @DelicateCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Themes.loadTheme(this)

        binding = AllVerseActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPrefTextSize = getSharedPreferences("text_size_prefs", Context.MODE_PRIVATE)
        currentTextSize = sharedPrefTextSize.getInt("text_size", 16) // Get the saved text size

        updateAdapterTextSize(currentTextSize, verseList)

        val translationList = loadTranslations() // Load translations from the respective JSON
        val commentaryList = loadCommentaries() // Load commentaries from the respective JSON

        // Initialize the adapter with translations
        val adapter = AllVerseAdapter(verseList, currentTextSize, translationList)
        binding.verseRecyclerView.adapter = adapter
        binding.verseRecyclerView.layoutManager = LinearLayoutManager(this)

        // Load the verses asynchronously
        GlobalScope.launch(Dispatchers.IO) {
            verseList = loadAllVerses()

            // Update the UI on the main thread
            withContext(Dispatchers.Main) {
                // Set the chapter details in the UI
                binding.verseRecyclerView.layoutManager = LinearLayoutManager(this@AllVerseActivity)
                binding.verseRecyclerView.adapter = AllVerseAdapter(verseList, currentTextSize, translationList)

                // Hide the ProgressBar once the verses are loaded

            }
        }

        binding.verseRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.verseRecyclerView.adapter = AllVerseAdapter(verseList, 16, translationList)

        searchView = binding.searchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val verseSearchResult = when {
                    newText.isNullOrBlank() -> verseList
                    else -> verseList.filter { verse ->
                        val titleMatch = verse.title.contains(newText, ignoreCase = true)
                        val textMatch = verse.text.contains(newText, ignoreCase = true)
                        val transliterationMatch = verse.transliteration.contains(newText, ignoreCase = true)
                        val chapterNumberMatch = verse.chapter_number.toString().contains(newText, ignoreCase = true)
                        val wordMeaningsMatch = verse.word_meanings.contains(newText, ignoreCase = true)

                        titleMatch || textMatch || transliterationMatch || chapterNumberMatch || wordMeaningsMatch
                    }
                }

                val translationSearchResult = translationList.values.filter { translation ->
                    translation.authorName.contains(newText!!, ignoreCase = true) ||
                            translation.description.contains(newText, ignoreCase = true)
                }

                val commentarySearchResult = commentaryList.values.filter { commentary ->
                    commentary.authorName.contains(newText!!, ignoreCase = true) ||
                            commentary.description.contains(newText, ignoreCase = true)
                }

                val filteredList = if (verseSearchResult.isNotEmpty() || translationSearchResult.isNotEmpty() || commentarySearchResult.isNotEmpty()) {
                    // Combine the results and get unique verse_ids
                    val verseIds = (verseSearchResult.map { it.verse_id } + translationSearchResult.map { it.verse_id } + commentarySearchResult.map {it.verse_id}).toSet()

                    // Filter the original verseList based on the verse_ids
                    verseList.filter { it.verse_id in verseIds }
                } else {
                    emptyList()
                }

                updateAdapterTextSize(currentTextSize, filteredList)
                return true
            }
        })
    }

    private fun loadTranslations(): Map<Int, Translation> {
        val jsonString = loadJsonFromAsset("translation.json")
        val translationListType = object : TypeToken<List<Translation>>() {}.type

        val translations = Gson().fromJson<List<Translation>>(jsonString, translationListType)

        return translations.associateBy { it.id }
    }

    private fun loadCommentaries(): Map<Int, Commentary> {
        val jsonString = loadJsonFromAsset("commentary.json")
        val commentaryListType = object : TypeToken<List<Commentary>>() {}.type

        val commentaries = Gson().fromJson<List<Commentary>>(jsonString, commentaryListType)

        // Convert the list of commentaries into a map for easy access
        return commentaries.associateBy { it.id}
    }

    private fun loadJsonFromAsset(fileName: String): String? {
        return try {
            applicationContext.assets.open(fileName).bufferedReader().use {
                it.readText()
            }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            null
        }
    }

    private fun loadAllVerses(): List<Verse> {
        val jsonString = loadJsonFromAsset("verse.json")
        val verseListType = object : TypeToken<List<Verse>>() {}.type

        return Gson().fromJson(jsonString, verseListType)
    }

    private fun updateAdapterTextSize(newSize: Int, filteredList: List<Verse>) {
        val recyclerViewC = binding.verseRecyclerView
        val adapterC = recyclerViewC.adapter as? AllVerseAdapter
        adapterC?.updateTextSize(newSize, filteredList)
    }
}
