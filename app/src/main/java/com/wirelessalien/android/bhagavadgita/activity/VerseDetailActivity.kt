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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wirelessalien.android.bhagavadgita.R
import com.wirelessalien.android.bhagavadgita.adapter.CommentaryAdapter
import com.wirelessalien.android.bhagavadgita.adapter.CustomSpinnerAdapter
import com.wirelessalien.android.bhagavadgita.adapter.TranslationAdapter
import com.wirelessalien.android.bhagavadgita.data.Chapter
import com.wirelessalien.android.bhagavadgita.data.Commentary
import com.wirelessalien.android.bhagavadgita.data.FavouriteVerse
import com.wirelessalien.android.bhagavadgita.data.Translation
import com.wirelessalien.android.bhagavadgita.data.Verse
import com.wirelessalien.android.bhagavadgita.databinding.ActivityVerseDetailBinding
import com.wirelessalien.android.bhagavadgita.utils.Frequency
import com.wirelessalien.android.bhagavadgita.utils.Themes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import kotlin.math.abs

class VerseDetailActivity : AppCompatActivity() {
    private var currentVerseIndex = 0
    private lateinit var verses: List<Verse>
    private lateinit var mediaPlayer: MediaPlayer
    private var isPlaying = false
    private lateinit var binding: ActivityVerseDetailBinding
    private lateinit var gestureDetector: GestureDetector
    private lateinit var translations: List<Translation>
    private lateinit var selectedAuthor: String
    private lateinit var commentary: List<Commentary>
    private lateinit var selectedLanguageC: String
    private var currentTextSize: Int = 16
    private val frequency = Frequency()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Themes.loadTheme(this)

        binding = ActivityVerseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("author_prefs", Context.MODE_PRIVATE)

        val sharedPrefTextSize = getSharedPreferences("text_size_prefs", Context.MODE_PRIVATE)
        currentTextSize = sharedPrefTextSize.getInt("text_size", 16) // Get the saved text size

        gestureDetector = GestureDetector(this, MyGestureListener())

        binding.scrollView.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }

        mediaPlayer = MediaPlayer()
        verses = emptyList()
        commentary = emptyList()
        translations = emptyList()

        updateTextSize(currentTextSize)
        updateAdapterTextSize(currentTextSize)

        // Retrieve verse details and chapter number from intent extras
        val chapterNumber = intent.getIntExtra("chapter_number", 0)
        val verseTitle = intent.getStringExtra("verse_title")
        val verseText = intent.getStringExtra("verse_text")
        val verseTransliteration = intent.getStringExtra("verse_transliteration")
        val verseWordMeanings = intent.getStringExtra("verse_word_meanings")
        val textSize = currentTextSize

        lifecycleScope.launch {
            try {
                translations = withContext(Dispatchers.IO) {
                    getTranslationsFromJson("translation.json")
                }

                // Find all available authors from the translations
                val allAuthors = translations.map { it.authorName }.distinct()
                val authorSpinner = binding.authorSpinner

                // Use CustomSpinnerAdapter with coroutines
                val adapterA = withContext(Dispatchers.Main) {
                    CustomSpinnerAdapter(
                        this@VerseDetailActivity,
                        android.R.layout.simple_spinner_item,
                        allAuthors,
                        textSize
                    )
                }

                adapterA.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                authorSpinner.adapter = adapterA

                val savedAuthor = sharedPref.getString("selectedAuthor", "")
                val savedAuthorPosition = allAuthors.indexOf(savedAuthor)

                if (savedAuthorPosition != -1) {
                    authorSpinner.setSelection(savedAuthorPosition)
                }

                authorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        selectedAuthor = allAuthors[position]
                        sharedPref.edit().putString("selectedAuthor", selectedAuthor).apply()

                        updateTranslationList()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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

                updateTextSize(newSize)

                updateAdapterTextSize(newSize)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        lifecycleScope.launch {
            try {
                commentary = withContext(Dispatchers.IO) {
                    getCommentaryFromJson("commentary.json")
                }

                val allLanguage = commentary.map { it.lang }.distinct()
                val languageSpinner = binding.cAuthorSpinner

                // Use CustomSpinnerAdapter with coroutines
                val adapterL = withContext(Dispatchers.Main) {
                    CustomSpinnerAdapter(
                        this@VerseDetailActivity,
                        android.R.layout.simple_spinner_item,
                        allLanguage,
                        textSize
                    )
                }

                adapterL.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                languageSpinner.adapter = adapterL

                val savedLang = sharedPref.getString("selectedLang", "")
                val savedLangPosition = allLanguage.indexOf(savedLang)

                if (savedLangPosition != -1) {
                    languageSpinner.setSelection(savedLangPosition)
                }

                languageSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            selectedLanguageC = allLanguage[position]
                            sharedPref.edit().putString("selectedLang", selectedLanguageC).apply()
                            updateCommentaryList()
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Update the layout with the verse details
        binding.verseTitleTextView.text = verseTitle
        binding.verseContentTextView.text = verseText
        binding.verseTransliterationTextView.text = verseTransliteration
        binding.verseWordMeaningsTextView.text = verseWordMeanings

        verses = getVerses(chapterNumber)

        // Find the index of the selected verse in the list of verses
        val selectedVerseIndex = verses.indexOfFirst { it.title == verseTitle }
        if (selectedVerseIndex != -1) {
            currentVerseIndex = selectedVerseIndex
        }

        binding.nextChapterButton.setOnClickListener {
            val nextChapterNumber = chapterNumber + 1
            if (nextChapterNumber <= 18) {
                val nextChapterDetails = getChapterDetails(nextChapterNumber)
                val intent = Intent(this, ChapterDetailsActivity::class.java).apply {
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

        binding.shareButton.setOnClickListener {
            shareText()
        }
        binding.copyButton.setOnClickListener {
            copyText()
        }
        binding.favButton.setOnClickListener {
            onFavoriteButtonClick()
        }
        binding.readMRadioBtn.isChecked = isVerseRead()

        // Set a listener on the switch
        binding.readMRadioBtn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                markVerseAsRead()
                Toast.makeText(this, "Marked as Read", Toast.LENGTH_SHORT).show()
            } else {
                markVerseAsUnread()
            }
        }

        binding.viewTranslationButton.setOnClickListener {
            val currentVerseNumber = verses[currentVerseIndex].verse_id
            val intent = Intent(this, VerseTranslationActivity::class.java)
            intent.putExtra("verse_id", currentVerseNumber)
            startActivity(intent)
        }

        binding.playPauseButton.setOnClickListener {
            val audioUrl =
                "https://github.com/WirelessAlien/gita/raw/main/data/verse_recitation/${verses[currentVerseIndex].chapter_number}/${verses[currentVerseIndex].verse_number}.mp3"
            if (isPlaying) {
                pauseAudio()
            } else {
                playAudio(audioUrl, binding.progressBar)
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progressValue: Int,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    mediaPlayer.seekTo(progressValue)
                } else {
                    binding.seekBar.progress = mediaPlayer.currentPosition
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
    }

    private fun isVerseRead(): Boolean {
        val sharedPreferences = getSharedPreferences("read_verses", Context.MODE_PRIVATE)
        val verseId = verses[currentVerseIndex].verse_id
        return sharedPreferences.getBoolean("$verseId", false)
    }

    private fun markVerseAsRead() {
        val sharedPreferences = getSharedPreferences("read_verses", Context.MODE_PRIVATE)
        val verseId = verses[currentVerseIndex].verse_id
        val chapterNumber = verses[currentVerseIndex].chapter_number
        sharedPreferences.edit().apply {
            putBoolean("$verseId", true)
            putInt("$verseId-chapter", chapterNumber)
            apply()
        }
    }

    private fun markVerseAsUnread() {
        val sharedPreferences = getSharedPreferences("read_verses", Context.MODE_PRIVATE)
        val verseId = verses[currentVerseIndex].verse_id
        sharedPreferences.edit().putBoolean("$verseId", false).apply()
    }

    private fun onFavoriteButtonClick() {
        // Get the text elements you want to save
        val chapterId = verses[currentVerseIndex].chapter_number
        val verseTitle = binding.verseTitleTextView.text.toString()
        val verseContent = binding.verseContentTextView.text.toString()
        val transliteration = binding.verseTransliterationTextView.text.toString()
        val wordMeanings = binding.verseWordMeaningsTextView.text.toString()

        val translationRecyclerView = binding.translationRecyclerView
        val translationAdapter = translationRecyclerView.adapter as TranslationAdapter
        val translationText = translationAdapter.getAllTranslationText()

        val commentaryRecyclerView = binding.commentaryRecyclerView
        val commentaryAdapter = commentaryRecyclerView.adapter as CommentaryAdapter
        val commentaryText = commentaryAdapter.getAllCommentaryText()

        // Retrieve the existing list of favorites from SharedPreferences
        val sharedPreferences = getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val gson = Gson()
        val favoritesJson = sharedPreferences.getString("favoriteList", "[]")
        val favoriteListType = object : TypeToken<List<FavouriteVerse>>() {}.type
        val favoriteList =
            gson.fromJson<List<FavouriteVerse>>(favoritesJson, favoriteListType).toMutableList()

        // Add the new favorite item to the list
        val newFavoriteItem = FavouriteVerse(
            chapterId,
            verseTitle,
            verseContent,
            transliteration,
            wordMeanings,
            translationText,
            commentaryText
        )
        favoriteList.add(newFavoriteItem)

        // Save the updated list of favorites back to SharedPreferences
        val editor = sharedPreferences.edit()
        val updatedFavoritesJson = gson.toJson(favoriteList)
        editor.putString("favoriteList", updatedFavoritesJson)
        editor.apply()

        // Display a message or update UI to indicate that it's saved as a favorite
        Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show()
    }

    private fun updateTextSize(newSize: Int) {

        currentTextSize = newSize
        val textViewList = listOf(
            binding.verseTitleTextView,
            binding.verseContentTextView,
            binding.verseTransliterationTextView,
            binding.verseWordMeaningsTextView
            // Add other TextViews in your layout that you want to update
        )

        textViewList.forEach { textView ->
            textView.textSize = newSize.toFloat()
        }

        val sharedPrefTextSize = getSharedPreferences("text_size_prefs", Context.MODE_PRIVATE)
        sharedPrefTextSize.edit().putInt("text_size", newSize).apply()
    }

    private fun updateAdapterTextSize(newSize: Int) {
        // Notify the RecyclerView adapter to update text size
        val recyclerViewT = binding.translationRecyclerView
        val adapterT = recyclerViewT.adapter as? TranslationAdapter
        adapterT?.updateTextSize(newSize)

        val recyclerViewC = binding.commentaryRecyclerView
        val adapterC = recyclerViewC.adapter as? CommentaryAdapter
        adapterC?.updateTextSize(newSize)

        val customAdapterC = binding.authorSpinner.adapter as? CustomSpinnerAdapter
        customAdapterC?.textSize = newSize // Int value directly
        customAdapterC?.notifyDataSetChanged()

        val customAdapterT = binding.cAuthorSpinner.adapter as? CustomSpinnerAdapter
        customAdapterT?.textSize = newSize // Int value directly
        customAdapterT?.notifyDataSetChanged()


    }

    private fun getTranslationsFromJson(fileName: String): List<Translation> {
        val jsonString = getJsonDataFromAsset(fileName)
        val listTranslationType = object : TypeToken<List<Translation>>() {}.type
        return Gson().fromJson(jsonString, listTranslationType)
    }

    private fun getCommentaryFromJson(fileName: String): List<Commentary> {
        val jsonString = getJsonDataFromAsset(fileName)
        val listCommentaryType = object : TypeToken<List<Commentary>>() {}.type
        return Gson().fromJson(jsonString, listCommentaryType)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    private inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {
        private val swipeThreshold = 100
        private val swipeVelocityThreshold = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val distanceX = e2.x - (e1?.x ?: 0F)
            val distanceY = e2.y - (e1?.y ?: 0F)

            if (abs(distanceX) > abs(distanceY) &&
                abs(distanceX) > swipeThreshold &&
                abs(velocityX) > swipeVelocityThreshold
            ) {
                if (distanceX > 0) {
                    onSwipe(SwipeDirection.RIGHT)
                } else {
                    onSwipe(SwipeDirection.LEFT)
                }
                Log.i("Fling", "Fling detected at ${System.currentTimeMillis()}")
                return true
            }

            return false
        }
    }

    private fun hapticFeedback(inBound: Boolean) {
        val feedbackType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (inBound) HapticFeedbackConstants.GESTURE_START else HapticFeedbackConstants.GESTURE_END
        } else {
            HapticFeedbackConstants.VIRTUAL_KEY
        }
        binding.root.performHapticFeedback(feedbackType)
    }

    enum class SwipeDirection {
        LEFT, RIGHT
    }

    private fun onSwipe(direction: SwipeDirection) {
        val (isValidSwipe, isChapterInBound, toastMessage) = when (direction) {
            SwipeDirection.RIGHT -> {
                if (currentVerseIndex > 0) {
                    currentVerseIndex--
                    Triple(true, true, null)
                } else {
                    Triple(false, false, "You have reached the first verse of this chapter")
                }
            }
            SwipeDirection.LEFT -> {
                if (currentVerseIndex < verses.size - 1) {
                    currentVerseIndex++
                    if (currentVerseIndex == verses.size - 1) {
                        binding.nextChapterButton.visibility = View.VISIBLE
                    }
                    Triple(true, true, null)
                } else {
                    Triple(false, false, "You have reached the last verse of this chapter")
                }
            }
        }

        lifecycleScope.launch {
            frequency.throttle(500) { hapticFeedback(isChapterInBound) }
            if (isValidSwipe) {
                handleVerseChange()
            } else {
                Toast.makeText(this@VerseDetailActivity, toastMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleVerseChange() {
        pauseAudio()
        val currentVerse = verses[currentVerseIndex]
        updateVerseDetails(binding, currentVerse)
        updateTranslationList()
        updateCommentaryList()
        updateAdapterTextSize(currentTextSize)
        binding.readMRadioBtn.isChecked = isVerseRead()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.root.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        }
    }

    private fun updateTranslationList() {
        // Filter the list of translations based on the selected author and verse number
        val filteredTranslations = translations.filter {
            it.authorName == selectedAuthor && it.verse_id == verses[currentVerseIndex].verse_id
        }

        val translationRecyclerView = binding.translationRecyclerView
        val translationAdapter = TranslationAdapter(filteredTranslations, currentTextSize)
        translationRecyclerView.adapter = translationAdapter
        translationRecyclerView.layoutManager = LinearLayoutManager(this)

    }

    private fun updateCommentaryList() {

        val filteredCommentary = commentary.filter {
            it.lang == selectedLanguageC && it.verse_id == verses[currentVerseIndex].verse_id
        }

        // Set up the RecyclerView to display the filtered translations
        val commentaryRecyclerView = binding.commentaryRecyclerView
        val commentaryAdapter = CommentaryAdapter(filteredCommentary, currentTextSize)
        commentaryRecyclerView.adapter = commentaryAdapter
        commentaryRecyclerView.layoutManager = LinearLayoutManager(this)

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

    private fun updateVerseDetails(binding: ActivityVerseDetailBinding, verse: Verse) {
        binding.verseTitleTextView.text = verse.title
        binding.verseContentTextView.text = verse.text
        binding.verseTransliterationTextView.text = verse.transliteration
        binding.verseWordMeaningsTextView.text = verse.word_meanings
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

    private fun getAllTextContent(): String {
        val verseTitle = binding.verseTitleTextView.text.toString()
        val verseContent = binding.verseContentTextView.text.toString()
        val verseTransliteration = binding.verseTransliterationTextView.text.toString()
        val verseWordMeanings = binding.verseWordMeaningsTextView.text.toString()

        val translationRecyclerView = binding.translationRecyclerView
        val translationAdapter = translationRecyclerView.adapter as TranslationAdapter
        val translationText = translationAdapter.getAllTranslationText()

        val commentaryRecyclerView = binding.commentaryRecyclerView
        val commentaryAdapter = commentaryRecyclerView.adapter as CommentaryAdapter
        val commentaryText = commentaryAdapter.getAllCommentaryText()

        // Combine all the text content into one string
        val textToShare = """
        Verse Title: $verseTitle
        Verse Content: $verseContent
        Verse Transliteration: $verseTransliteration
        Verse Word Meanings: $verseWordMeanings

        Translations:
        $translationText

        Commentary:
        $commentaryText
    """.trimIndent()

        // Add your desired line of text at the end
        val additionalText =
            "Shared from - Bhagavad Gita App(https://github.com/WirelessAlien/BhagavadGitaApp)"

        return "$textToShare\n$additionalText"
    }

    private fun copyText() {
        val textToCopy = getAllTextContent()

        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val clipData = ClipData.newPlainText("Text to Copy", textToCopy)
        clipboardManager.setPrimaryClip(clipData)

        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
    }


    private fun shareText() {
        val textToShare = getAllTextContent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, textToShare)
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }


    private fun playAudio(audioUrl: String, progressBar: ProgressBar) {
        CoroutineScope(Dispatchers.IO).launch {
            val fileName = audioUrl.substringAfterLast("/")
            val cacheDir = applicationContext.cacheDir
            val subDirName = "audio_cache"
            val chapterNumber = verses[currentVerseIndex].chapter_number
            val subDir = File(cacheDir, "$subDirName/chapter_$chapterNumber")

            if (!subDir.exists()) {
                subDir.mkdirs()
            }

            val file = File(subDir, fileName)
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.VISIBLE
                progressBar.progress = 0
            }

            try {
                withContext(Dispatchers.IO) {
                    if (!file.exists()) {
                        val urlConnection = URL(audioUrl).openConnection()
                        val contentLength = urlConnection.contentLength
                        val inputStream = urlConnection.getInputStream()
                        val outputStream = FileOutputStream(file)
                        val buffer = ByteArray(1024)
                        var totalBytesRead = 0
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            val progress = (totalBytesRead * 100 / contentLength)
                            withContext(Dispatchers.Main) {
                                progressBar.progress = progress
                            }
                        }
                        inputStream.close()
                        outputStream.close()
                    }
                }
                withContext(Dispatchers.Main) {
                    mediaPlayer.release()
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(file.absolutePath)
                        prepare()
                        start()
                    }
                    isPlaying = true
                    progressBar.visibility = View.GONE
                    updatePlayPauseButton()
                    startSeekBarUpdate()
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        applicationContext,
                        "Unable to play the audio. Please check your internet connection.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun pauseAudio() {
        mediaPlayer.pause()
        isPlaying = false
        updatePlayPauseButton()
    }

    private fun updatePlayPauseButton() {
        if (isPlaying) {
            binding.playPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            binding.playPauseButton.setImageResource(R.drawable.ic_play)
        }
    }

    private fun startSeekBarUpdate() {
        binding.seekBar.max = mediaPlayer.duration
        val handler = Handler(Looper.getMainLooper())

        val updateInterval = 100

        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    binding.seekBar.progress = mediaPlayer.currentPosition
                    handler.postDelayed(this, updateInterval.toLong())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, 0)
    }

    override fun onStop() {
        super.onStop()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.pause()
            isPlaying = false
            updatePlayPauseButton()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}