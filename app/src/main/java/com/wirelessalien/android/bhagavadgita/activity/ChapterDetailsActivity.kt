
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

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wirelessalien.android.bhagavadgita.adapter.VerseAdapter
import com.wirelessalien.android.bhagavadgita.data.Verse
import com.wirelessalien.android.bhagavadgita.databinding.ActivityChapterDetailBinding
import kotlinx.coroutines.*
import java.io.IOException

class ChapterDetailActivity : AppCompatActivity() {

    private var verseList: List<Verse> = emptyList()
    private var isSummaryExpanded = false
    private var isSummaryHindiExpanded = false
    private lateinit var progressBar: ProgressBar

    @DelicateCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityChapterDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressBar = binding.progressBar

        // Retrieve the chapter details from the intent
        val chapterNumber = intent.getIntExtra("chapter_number", 0)
        val chapterName = intent.getStringExtra("chapter_name")
        val chapterNameMeaning = intent.getStringExtra("name_meaning")
        val chapterSummary = intent.getStringExtra("chapter_summary")
        val chapterSummaryHindi = intent.getStringExtra("chapter_summary_hindi")

        progressBar.visibility = View.VISIBLE

        // Load the verses asynchronously
        GlobalScope.launch(Dispatchers.IO) {
            verseList = loadVersesForChapter(chapterNumber)

            // Update the UI on the main thread
            withContext(Dispatchers.Main) {
                // Set the chapter details in the UI
                binding.chapterNumberTextView.text = chapterNumber.toString()
                binding.chapterNameTextView.text = chapterName
                binding.chapterNameMeaningTextView.text = chapterNameMeaning
                binding.verseRecyclerView.layoutManager = LinearLayoutManager(this@ChapterDetailActivity)
                binding.verseRecyclerView.adapter = VerseAdapter(verseList)

                // Hide the ProgressBar once the verses are loaded
                progressBar.visibility = View.GONE
            }
        }

        // Show only two lines of the English version of the chapter summary initially
        binding.chapterSummaryTextView.text = getEllipsizedText(chapterSummary ?: "", 2, 40)
        binding.seeMoreTextView.setOnClickListener {
            isSummaryExpanded = !isSummaryExpanded
            if (isSummaryExpanded) {
                binding.chapterSummaryTextView.text = chapterSummary
                binding.seeMoreTextView.text = "See Less"
            } else {
                binding.chapterSummaryTextView.text = getEllipsizedText(chapterSummary ?: "", 2, 40)
                binding.seeMoreTextView.text = "See More"
            }
        }

        // Show only two lines of the Hindi version of the chapter summary initially
        binding.chapterSummaryHindiTextView.text = getEllipsizedText(chapterSummaryHindi ?: "", 2, 40)
        binding.seeMoreHindiTextView.setOnClickListener {
            isSummaryHindiExpanded = !isSummaryHindiExpanded
            if (isSummaryHindiExpanded) {
                binding.chapterSummaryHindiTextView.text = chapterSummaryHindi
                binding.seeMoreHindiTextView.text = "छोटा करें"
            } else {
                binding.chapterSummaryHindiTextView.text = getEllipsizedText(chapterSummaryHindi ?: "", 2, 40)
                binding.seeMoreHindiTextView.text = "और देखें"
            }
        }

        binding.verseRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.verseRecyclerView.adapter = VerseAdapter(verseList)
    }

    private fun getEllipsizedText(text: String, maxLines: Int, maxCharactersPerLine: Int): String {
        val maxCharacters = maxLines * maxCharactersPerLine
        return if (text.length > maxCharacters) {
            "${text.substring(0, maxCharacters)}..."
        } else {
            text
        }
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

    private fun loadVersesForChapter(chapterNumber: Int): List<Verse> {
        val jsonString = loadJsonFromAsset("verse.json")
        val verseListType = object : TypeToken<List<Verse>>() {}.type

        val allVerses: List<Verse> = Gson().fromJson(jsonString, verseListType)

        return allVerses.filter { it.chapter_number == chapterNumber }
    }
}

