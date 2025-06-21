
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

package com.wirelessalien.android.bhagavadgita

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.color.DynamicColors
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wirelessalien.android.bhagavadgita.activity.AboutGitaActivity
import com.wirelessalien.android.bhagavadgita.activity.AllVerseActivity
import com.wirelessalien.android.bhagavadgita.activity.ChapterDetailsActivity
import com.wirelessalien.android.bhagavadgita.activity.FavouriteActivity
import com.wirelessalien.android.bhagavadgita.activity.HanumanChalisaActivity
import com.wirelessalien.android.bhagavadgita.activity.SettingsActivity
import com.wirelessalien.android.bhagavadgita.activity.VerseDetailActivity
import com.wirelessalien.android.bhagavadgita.adapter.ChapterAdapter
import com.wirelessalien.android.bhagavadgita.adapter.SearchResultsAdapter // Added import
import com.wirelessalien.android.bhagavadgita.adapter.SliderVerseAdapter
import com.wirelessalien.android.bhagavadgita.data.Chapter
import com.wirelessalien.android.bhagavadgita.data.Verse
import com.wirelessalien.android.bhagavadgita.databinding.ActivityMainBinding
import com.wirelessalien.android.bhagavadgita.fragment.AboutAppFragment
import com.wirelessalien.android.bhagavadgita.utils.Themes
import org.json.JSONArray
import java.io.IOException
import java.nio.charset.Charset
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var chapterList: List<Chapter>
    private lateinit var initialVerseList: List<Verse>
    private lateinit var allVersesForSearch: List<Verse>
    private var currentTextSize: Int = 16 // Default text size
    private lateinit var searchResultsAdapter: SearchResultsAdapter
    private val searchResults = mutableListOf<Any>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Themes.loadTheme(this)

        val sharedPrefTextSize = PreferenceManager.getDefaultSharedPreferences(this)
        currentTextSize = sharedPrefTextSize.getInt("text_size_preference", 16) // Get the saved text size

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        DynamicColors.applyToActivityIfAvailable(this)

        allVersesForSearch = loadVersesFromJson() // Load all verses for search
        initialVerseList = allVersesForSearch.shuffled(Random(System.currentTimeMillis())) // Shuffled list for slider

        // Load JSON data from assets for chapters
        val jsonString = applicationContext.assets.open("chapters.json").bufferedReader().use {
            it.readText()
        }
        chapterList = parseJson(jsonString)

        // Setup main RecyclerView for chapters
        val chapterAdapter = ChapterAdapter(chapterList, currentTextSize)
        binding.recyclerView.adapter = chapterAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        updateAdapterTextSize(currentTextSize)

        // Setup SearchBar and SearchView
        binding.searchView.setupWithSearchBar(binding.searchBar)
        searchResultsAdapter = SearchResultsAdapter(searchResults, this::onSearchResultClicked)
        binding.searchResultsRecyclerView.adapter = searchResultsAdapter
        binding.searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)

        binding.searchView.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                performSearch(s.toString())
            }
        })

        binding.searchView.editText.setOnEditorActionListener { v, actionId, event ->
            binding.searchBar.setText(binding.searchView.text)

            true
        }

        binding.searchBar.inflateMenu(R.menu.main);
        binding.searchBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.nav_about_gita -> {
                    intent.setClass(this, AboutGitaActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_hanuman_chalisa -> {
                    intent.setClass(this, HanumanChalisaActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_theme -> {
                    intent.setClass(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_about -> {
                    val aboutDialog = AboutAppFragment()
                    aboutDialog.show(supportFragmentManager, "AboutAppFragment")

                }
                R.id.nav_fav -> {
                    intent.setClass(this, FavouriteActivity::class.java)
                    startActivity(intent)
                }
            }
            false
        }


        // Setup ViewPager for initial verses
        val sliderAdapter = SliderVerseAdapter(initialVerseList)
        binding.viewPager.adapter = sliderAdapter

        // Auto slide after every 10 seconds
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (initialVerseList.isNotEmpty()) {
                    binding.viewPager.currentItem = (binding.viewPager.currentItem + 1) % initialVerseList.size
                    handler.postDelayed(this, 10000)
                }
            }
        }
        if (initialVerseList.isNotEmpty()) {
            handler.postDelayed(runnable, 10000)
        }


        val progressValue = when (currentTextSize) {
            16 -> 0
            20 -> 1
            24 -> 2
            28 -> 3
            32 -> 4
            else -> 1 // Default text size
        }

        binding.textSizeSeekBar.progress = progressValue

        val textSizeSeekBar = binding.textSizeSeekBar
        textSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Update the text size when the SeekBar progress changes
                val newSize = when (progress) {
                    0 -> 16
                    1 -> 20
                    2 -> 24
                    3 -> 28
                    4 -> 32
                    else -> 16 // Default text size
                }

                updateAdapterTextSize(newSize)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.hanumanChalisaText.setOnClickListener {
            val intent = Intent(this, HanumanChalisaActivity::class.java)
            startActivity(intent)
        }

        binding.btnAllVerse.setOnClickListener {
            val intent = Intent(this, AllVerseActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performSearch(query: String?) {
        searchResults.clear()
        if (query.isNullOrBlank()) {

            binding.searchResultsRecyclerView.visibility = View.GONE
        } else {
            binding.searchResultsRecyclerView.visibility = View.VISIBLE
            val lowerCaseQuery = query.lowercase()
            Log.d("Search", "Performing search for: $lowerCaseQuery")

            // Search in Chapters
            val filteredChapters = chapterList.filter { chapter ->
                chapter.name.lowercase().contains(lowerCaseQuery) ||
                        chapter.name_meaning.lowercase().contains(lowerCaseQuery) ||
                        chapter.name_translation.lowercase().contains(lowerCaseQuery) ||
                        chapter.name_transliterated.lowercase().contains(lowerCaseQuery) ||
                        chapter.chapter_summary.lowercase().contains(lowerCaseQuery) ||
                        chapter.chapter_summary_hindi.lowercase().contains(lowerCaseQuery)
            }
            searchResults.addAll(filteredChapters)

            // Search in Verses (using allVersesForSearch)
            val filteredVerses = allVersesForSearch.filter { verse ->
                verse.text.lowercase().contains(lowerCaseQuery) || verse.transliteration.lowercase().contains(lowerCaseQuery) || verse.meaning?.en?.lowercase()?.contains(lowerCaseQuery) == true || verse.meaning?.hi?.lowercase()?.contains(lowerCaseQuery) == true
                        || verse.verse_number.toString().contains(lowerCaseQuery) || verse.id.toString().contains(lowerCaseQuery)
            }
            searchResults.addAll(filteredVerses)
        }
        searchResultsAdapter.updateData(searchResults.toList())
    }

    private fun onSearchResultClicked(item: Any) {
        when (item) {
            is Chapter -> {
                val intent = Intent(this, ChapterDetailsActivity::class.java).apply {
                    putExtra("chapter_number", item.chapter_number)
                    putExtra("chapter_name", item.name)
                    putExtra("name_meaning", item.name_meaning)
                    putExtra("chapter_summary", item.chapter_summary)
                    putExtra("chapter_summary_hindi", item.chapter_summary_hindi)
                    putExtra("verses_count", item.verses_count)
                }
                startActivity(intent)
            }
            is Verse -> {
                val intent = Intent(this, VerseDetailActivity::class.java).apply {
                    putExtra("verse_id", item.id)
                    putExtra("chapter_number", item.chapter_number)
                    putExtra("verse_number", item.verse_number)
                    putExtra("text", item.text)
                    putExtra("transliteration", item.transliteration)
                    putExtra("meaning_en", item.meaning?.en ?: "")
                    putExtra("meaning_hi", item.meaning?.hi ?: "")


                }
                startActivity(intent)
                Log.d("Search", "Clicked on Verse: ${item.id}")
            }
        }
        // binding.searchView.hide()
        // binding.searchBar.setText(if (item is Chapter) item.name else if (item is Verse) "Verse ${item.verse_number}")
    }


    override fun onResume() {
        super.onResume()
        val adapterC = binding.recyclerView.adapter as? ChapterAdapter
        adapterC?.updateProgressData()
    }

    private fun updateAdapterTextSize(newSize: Int) {

        val recyclerViewC = binding.recyclerView
        val adapterC = recyclerViewC.adapter as? ChapterAdapter
        adapterC?.updateTextSize(newSize)
        // TODO: Update text size for search results adapter if needed

        val sharedPrefTextSize= getSharedPreferences("text_size_prefs", Context.MODE_PRIVATE)
        sharedPrefTextSize.edit().putInt("text_size", newSize).apply()
    }
    private fun loadVersesFromJson(): List<Verse> {
        val json: String?
        try {
            val inputStream = assets.open("verse.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charset.defaultCharset())
        } catch (e: IOException) {
            e.printStackTrace()
            return emptyList()
        }
        val listType = object : TypeToken<List<Verse>>() {}.type
        return Gson().fromJson(json, listType)
    }
    private fun parseJson(jsonString: String): List<Chapter> {
        val chapterList = mutableListOf<Chapter>()
        val jsonArray = JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val chapterJson = jsonArray.getJSONObject(i)
            val chapter = Chapter(
                chapterJson.getInt("chapter_number"),
                chapterJson.getString("chapter_summary"),
                chapterJson.getString("chapter_summary_hindi"),
                chapterJson.getInt("id"),
                chapterJson.getString("image_name"),
                chapterJson.getString("name"),
                chapterJson.getString("name_meaning"),
                chapterJson.getString("name_translation"),
                chapterJson.getString("name_transliterated"),
                chapterJson.getInt("verses_count")
            )
            chapterList.add(chapter)
        }
        return chapterList
    }
}