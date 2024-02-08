package com.wirelessalien.android.bhagavadgita.activity

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
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

class AllVerseActivity : AppCompatActivity() {

    private lateinit var binding: AllVerseActivityBinding
    private var verseList: List<Verse> = emptyList()
    private var currentTextSize: Int = 16
    private lateinit var searchView: SearchView
    private lateinit var adapter: AllVerseAdapter
    private lateinit var translationList: Map<Int, Translation>
    private lateinit var commentaryList: Map<Int, Commentary>

    @DelicateCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Themes.loadTheme(this)

        binding = AllVerseActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UI components
        initUI()

        // Load translations and commentaries
        translationList = loadTranslations()
        commentaryList = loadCommentaries()

        // Initialize the adapter with translations
        adapter = AllVerseAdapter(verseList, currentTextSize)
        binding.verseRecyclerView.adapter = adapter
        binding.verseRecyclerView.layoutManager = LinearLayoutManager(this)

        // Load the verses asynchronously
        loadVersesAsync()

        // Set up search functionality
        setupSearchView()
    }

    private fun initUI() {
        val sharedPrefTextSize =
            getSharedPreferences("text_size_prefs", Context.MODE_PRIVATE)
        currentTextSize = sharedPrefTextSize.getInt("text_size", 16)
        updateAdapterTextSize(currentTextSize, verseList, "", emptyList())
    }

    private fun loadVersesAsync() {
        // Show ProgressBar
        binding.progressBar.visibility = View.VISIBLE

        // Load verses asynchronously
        lifecycleScope.launch(Dispatchers.IO) {
            verseList = loadAllVerses()

            // Update the UI on the main thread
            withContext(Dispatchers.Main) {
                // Set the chapter details in the UI
                binding.verseRecyclerView.layoutManager = LinearLayoutManager(this@AllVerseActivity)
                binding.verseRecyclerView.adapter = AllVerseAdapter(verseList, currentTextSize)
                binding.progressBar.visibility = View.GONE // Hide the ProgressBar once the verses are loaded
            }
        }
    }

    private fun setupSearchView() {
        searchView = binding.searchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Perform search in the background
                searchInBackground(newText)
                return true
            }
        })
    }

    private fun searchInBackground(newText: String?) {
        lifecycleScope.launch(Dispatchers.IO) {
            val verseSearchResult = when {
                newText.isNullOrBlank() -> verseList
                else -> verseList.filter { verse ->
                    val titleMatch = verse.title.contains(newText, ignoreCase = true)
                    val textMatch = verse.text.contains(newText, ignoreCase = true)
                    val transliterationMatch =
                        verse.transliteration.contains(newText, ignoreCase = true)
                    val chapterNumberMatch =
                        verse.chapter_number.toString().contains(newText, ignoreCase = true)
                    val wordMeaningsMatch =
                        verse.word_meanings.contains(newText, ignoreCase = true)

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

            val matchedContexts = mutableListOf<String>()

           // Add matched contexts for verse titles
            verseSearchResult.forEach { verse ->
                if (verse.title.contains(newText!!, ignoreCase = true)) {
                    matchedContexts.add("Match found in verse title: ${verse.title}")
                }
            }

            // Add matched contexts for verse text
            verseSearchResult.forEach { verse ->
                if (verse.text.contains(newText!!, ignoreCase = true)) {
                    matchedContexts.add(verse.text)
                }
            }

            // Add matched contexts for chapter numbers
            verseSearchResult.forEach { verse ->
                if (verse.chapter_number.toString().contains(newText!!, ignoreCase = true)) {
                    matchedContexts.add("Match found in chapter: ${verse.chapter_number}")
                }
            }

            // Add matched contexts for transliteration
            verseSearchResult.forEach { verse ->
                if (verse.transliteration.contains(newText!!, ignoreCase = true)) {
                    matchedContexts.add("Match found in transliteration: ${verse.transliteration}")
                }
            }

            // Add matched contexts for word meanings
            verseSearchResult.forEach { verse ->
                if (verse.word_meanings.contains(newText!!, ignoreCase = true)) {
                    matchedContexts.add("Match found in word meanings: ${verse.word_meanings}")
                }
            }

            // Add matched contexts for translations
            translationSearchResult.forEach { translation ->
                if (translation.authorName.contains(newText!!, ignoreCase = true)) {
                    matchedContexts.add("Match found in translation author: ${translation.authorName}")
                }
                if (translation.description.contains(newText, ignoreCase = true)) {
                    matchedContexts.add("Match found in translation: ${translation.description}")
                }
            }

            // Add matched contexts for commentaries
            commentarySearchResult.forEach { commentary ->
                if (commentary.authorName.contains(newText!!, ignoreCase = true)) {
                    matchedContexts.add("Match found in commentary author: ${commentary.authorName}")
                }
                if (commentary.description.contains(newText, ignoreCase = true)) {
                    matchedContexts.add("Match found in commentary: ${commentary.description}")
                }
            }

            val matchedText = if (!newText.isNullOrBlank()) newText else null

            val filteredList = if (verseSearchResult.isNotEmpty() || translationSearchResult.isNotEmpty() || commentarySearchResult.isNotEmpty()) {
                // Combine the results and get unique verse_ids
                val verseIds = (verseSearchResult.map { it.verse_id } + translationSearchResult.map { it.verse_id } + commentarySearchResult.map { it.verse_id }).toSet()

                // Filter the original verseList based on the verse_ids
                verseList.filter { it.verse_id in verseIds }
            } else {
                emptyList()
            }

            val highlightedMatchedContexts = matchedContexts.map { context ->
                val spannableString = SpannableString(context)
                val startIndex = context.indexOf(newText!!, ignoreCase = true)
                if (startIndex != -1) {
                    val endIndex = startIndex + newText.length
                    spannableString.setSpan(
                        BackgroundColorSpan(Color.YELLOW), // Highlight color
                        startIndex, endIndex,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                spannableString
            }

            // Update UI on the main thread
            withContext(Dispatchers.Main) {
                updateAdapterTextSize(currentTextSize, filteredList, matchedText, highlightedMatchedContexts)
            }
        }
    }

    private fun updateAdapterTextSize(newSize: Int, filteredList: List<Verse>, matchedText: String?, matchedContexts: List<SpannableString>) {
        val recyclerViewC = binding.verseRecyclerView
        val adapterC = recyclerViewC.adapter as? AllVerseAdapter
        adapterC?.updateTextSize(newSize, filteredList, matchedText, matchedContexts)
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
}
