package com.wirelessalien.android.bhagavadgita.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wirelessalien.android.bhagavadgita.data.Chapter
import com.wirelessalien.android.bhagavadgita.data.Verse
import com.wirelessalien.android.bhagavadgita.databinding.ActivityVerseDetailBinding
import java.io.IOException

class VerseDetailActivity : AppCompatActivity() {
    private var currentVerseIndex = 0
    private lateinit var verses: List<Verse>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityVerseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve verse details and chapter number from intent extras
        val chapterNumber = intent.getIntExtra("chapter_number", 0)
        val verseTitle = intent.getStringExtra("verse_title")
        val verseText = intent.getStringExtra("verse_text")
        val verseTransliteration = intent.getStringExtra("verse_transliteration")
        val verseWordMeanings = intent.getStringExtra("verse_word_meanings")

        // Update the layout with the verse details
        binding.verseTitleTextView.text = verseTitle
        binding.verseContentTextView.text = verseText
        binding.verseTransliterationTextView.text = verseTransliteration
        binding.verseWordMeaningsTextView.text = verseWordMeanings

        // Retrieve the list of verses for the given chapter
        verses = getVerses(chapterNumber)

        // Find the index of the selected verse in the list of verses
        val selectedVerseIndex = verses.indexOfFirst { it.title == verseTitle }
        if (selectedVerseIndex != -1) {
            currentVerseIndex = selectedVerseIndex
        }

        binding.previousVerseButton.setOnClickListener {
            if (currentVerseIndex > 0) {

                currentVerseIndex--

                val prevVerse = verses[currentVerseIndex]

                // Update the layout with the details of the previous verse
                binding.verseTitleTextView.text = prevVerse.title
                binding.verseContentTextView.text = prevVerse.text
                binding.verseTransliterationTextView.text = prevVerse.transliteration
                binding.verseWordMeaningsTextView.text = prevVerse.word_meanings

                binding.nextVerseButton.isEnabled = true

                if (currentVerseIndex == 0) {
                    binding.previousVerseButton.visibility = View.INVISIBLE
                }

                binding.nextChapterButton.visibility = View.GONE
            }
        }

        // Set a click listener on the "Next Verse" button
        binding.nextVerseButton.setOnClickListener {
            if (currentVerseIndex < verses.size - 1) {
                currentVerseIndex++

                val nextVerse = verses[currentVerseIndex]

                // Update the layout with the details of the next verse
                binding.verseTitleTextView.text = nextVerse.title
                binding.verseContentTextView.text = nextVerse.text
                binding.verseTransliterationTextView.text = nextVerse.transliteration
                binding.verseWordMeaningsTextView.text = nextVerse.word_meanings

                // Check if the current verse is now the last verse of the chapter
                if (currentVerseIndex == verses.size - 1) {
                    binding.nextChapterButton.visibility = View.VISIBLE
                }
            } else {
                Toast.makeText(this, "You have reached the last verse of this chapter", Toast.LENGTH_SHORT).show()
                binding.nextVerseButton.isEnabled = false
            }
        }

        // Set a click listener on the "Next Chapter" button
        binding.nextChapterButton.setOnClickListener {
            // Calculate the chapter number of the next chapter
            val nextChapterNumber = chapterNumber + 1
            if (nextChapterNumber <= 18) {
                val nextChapterDetails = getChapterDetails(nextChapterNumber)

                // Start the ChapterDetailsActivity to display the details of the next chapter
                val intent = Intent(this, ChapterDetailActivity::class.java).apply {
                    putExtra("chapter_number", nextChapterDetails?.chapter_number ?: 0)
                    putExtra("chapter_name", nextChapterDetails?.name)
                    putExtra("name_meaning", nextChapterDetails?.name_meaning)
                    putExtra("chapter_summary", nextChapterDetails?.chapter_summary)
                    putExtra("chapter_summary_hindi", nextChapterDetails?.chapter_summary_hindi)
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "No more chapters available.", Toast.LENGTH_SHORT).show()
                binding.nextChapterButton.isEnabled = false
            }
        }

        binding.viewTranslationButton.setOnClickListener {
            // Get the current verse number
            val currentVerseNumber = currentVerseIndex

            // Create an intent to open the new activity
            val intent = Intent(this, VerseTranslationActivity::class.java)
            intent.putExtra("verseNumber", currentVerseNumber)
            startActivity(intent)
        }
    }

    private fun getChapterDetails(chapterNumber: Int): Chapter? {
        val jsonString = loadJsonFromAsset("chapters.json")
        val chapterListType = object : TypeToken<List<Chapter>>() {}.type
        val allChapters: List<Chapter> = Gson().fromJson(jsonString, chapterListType)

        return allChapters.find { it.chapter_number == chapterNumber }
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

    private fun getVerses(chapterNumber: Int): List<Verse> {
        val jsonString = loadJsonFromAsset("verse.json")
        val verseListType = object : TypeToken<List<Verse>>() {}.type

        val allVerses: List<Verse> = Gson().fromJson(jsonString, verseListType)

        return allVerses.filter { it.chapter_number == chapterNumber }
    }
}