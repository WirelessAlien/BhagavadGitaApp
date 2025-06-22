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
// import android.view.Menu // No longer needed
// import android.view.MenuItem // No longer needed
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
// import android.widget.SeekBar // No longer used
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
// FavouriteVerse is not directly used here for saving anymore, but might be used by other parts if not fully refactored.
// For now, we are creating FavoriteDbHelper and using it.
import com.wirelessalien.android.bhagavadgita.data.FavoriteDbHelper
import com.wirelessalien.android.bhagavadgita.data.Translation
import com.wirelessalien.android.bhagavadgita.data.Verse
import com.wirelessalien.android.bhagavadgita.databinding.ActivityVerseDetailBinding
import com.wirelessalien.android.bhagavadgita.utils.AudioUrlHelper
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
    private lateinit var audioTypes: List<AudioUrlHelper.AudioType>
    private lateinit var selectedAudioType: AudioUrlHelper.AudioType


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
                val authorAutoComplete = binding.authorAutoCompleteTextView

                val adapterA = ArrayAdapter(
                    this@VerseDetailActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    allAuthors
                )
                authorAutoComplete.setAdapter(adapterA)

                val savedAuthor = sharedPref.getString("selectedAuthor", "")
                if (allAuthors.contains(savedAuthor)) {
                    authorAutoComplete.setText(savedAuthor, false)
                    selectedAuthor = savedAuthor ?: ""
                    updateTranslationList() // Load initial translation
                }

                authorAutoComplete.setOnItemClickListener { parent, _, position, _ ->
                    selectedAuthor = parent.adapter.getItem(position) as String
                    sharedPref.edit().putString("selectedAuthor", selectedAuthor).apply()
                    updateTranslationList()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        lifecycleScope.launch {
            try {
                commentary = withContext(Dispatchers.IO) {
                    getCommentaryFromJson("commentary.json")
                }

                val allLanguage = commentary.map { it.lang }.distinct()
                val cAuthorAutoComplete = binding.cAuthorAutoCompleteTextView

                val adapterL = ArrayAdapter(
                    this@VerseDetailActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    allLanguage
                )
                cAuthorAutoComplete.setAdapter(adapterL)

                val savedLang = sharedPref.getString("selectedLang", "")
                if (allLanguage.contains(savedLang)) {
                    cAuthorAutoComplete.setText(savedLang, false)
                    selectedLanguageC = savedLang ?: "" // Initialize selectedLanguageC
                    updateCommentaryList() // Load initial commentary
                }

                cAuthorAutoComplete.setOnItemClickListener { parent, _, position, _ ->
                    selectedLanguageC = parent.adapter.getItem(position) as String
                    sharedPref.edit().putString("selectedLang", selectedLanguageC).apply()
                    updateCommentaryList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Update the layout with the verse details
        binding.verseTitleTextView.text = verseTitle
        binding.verseContentTextView.text = verseText
        binding.verseTransliterationTextView.text = verseTransliteration

        verses = getVerses(chapterNumber)

        // Find the index of the selected verse in the list of verses
        val selectedVerseIndex = verses.indexOfFirst { it.title == verseTitle }
        if (selectedVerseIndex != -1) {
            currentVerseIndex = selectedVerseIndex
        }
        // Update favorite button status after verses are loaded and currentVerseIndex is set
         updateFavoriteButtonStatus() // Removed this call

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


        // Initialize audio types
        audioTypes = AudioUrlHelper.audioOptions
        selectedAudioType = audioTypes.first() // Default to first audio type

        // Populate audio spinner
        val audioSourceAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            audioTypes.map { it.displayName } // Populate with display names
        )
        binding.audioSourceDropdown.setAdapter(audioSourceAdapter)
        binding.audioSourceDropdown.setOnItemClickListener { _, _, position, _ ->
            selectedAudioType = audioTypes[position]
            // If audio is playing, stop it as the source has changed
            if (isPlaying) {
                pauseAudio()
            }
            // Optionally, you could start playing the new source immediately,
            // but for now, we'll just set it and let the user press play.
        }

        binding.fabPlay.setOnClickListener {
            if (verses.isEmpty() || currentVerseIndex < 0 || currentVerseIndex >= verses.size) {
                Toast.makeText(this, "Verse data not loaded yet.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val currentVerse = verses[currentVerseIndex]
            val audioUrl = AudioUrlHelper.getAudioUrl(
                currentVerse.chapter_number,
                currentVerse.verse_number,
                selectedAudioType
            )

            if (audioUrl != null) {
                if (isPlaying) {
                    pauseAudio()
                } else {
                    playAudio(audioUrl, binding.progressBar, selectedAudioType, currentVerse.chapter_number, currentVerse.verse_number)
                }
            } else {
                Toast.makeText(this, "Could not determine audio URL.", Toast.LENGTH_SHORT).show()
            }
        }

        setupDockedToolbarActions() // New setup function name

        updateFavoriteButtonStatus()
    }

    // Toolbar-specific methods (setupToolbarAndFab, onCreateOptionsMenu, onOptionsItemSelected) will be removed.

    private fun setupDockedToolbarActions() {
        binding.actionButtonPrevious.setOnClickListener {
            navigateToPreviousVerse()
        }
        binding.actionButtonNext.setOnClickListener {
            navigateToNextVerse()
        }
        binding.actionButtonFavorite.setOnClickListener {
            onFavoriteButtonClick()
        }

        binding.actionButtonShare.setOnClickListener {
            shareText()
        }

        // FAB setup remains the same as it's a separate component
        binding.actionMarkAsRead.setOnClickListener {
            toggleReadStatus()
        }
        // Set initial FAB state based on read status
        updateFabReadStatus()
    }

    private fun navigateToPreviousVerse() {
        if (currentVerseIndex > 0) {
            currentVerseIndex--
            handleVerseChange()
            hapticFeedback(true)
        } else {
            Toast.makeText(this, "You have reached the first verse of this chapter", Toast.LENGTH_SHORT).show()
            hapticFeedback(false)
        }
    }

    private fun navigateToNextVerse() {
        if (currentVerseIndex < verses.size - 1) {
            currentVerseIndex++
            if (currentVerseIndex == verses.size - 1) {
                binding.nextChapterButton.visibility = View.VISIBLE // Keep this logic if next chapter button is still relevant
            }
            handleVerseChange()
            hapticFeedback(true)
        } else {
            Toast.makeText(this, "You have reached the last verse of this chapter", Toast.LENGTH_SHORT).show()
            hapticFeedback(false)
        }
    }

    private fun toggleReadStatus() {
        if (isVerseRead()) {
            markVerseAsUnread()
            Toast.makeText(this, "Marked as Unread", Toast.LENGTH_SHORT).show()
            binding.actionMarkAsRead.setIconResource(R.drawable.ic_check_2)
        } else {
            markVerseAsRead()
            Toast.makeText(this, "Marked as Read", Toast.LENGTH_SHORT).show()
            binding.actionMarkAsRead.setIconResource(R.drawable.ic_check)
        }
        updateFabReadStatus()
    }

    private fun updateFabReadStatus() {
        if (isVerseRead()) {
            // Consider changing icon or tint if you have different icons for read/unread states
            // For now, we just use the single 'ic_check'
            binding.actionMarkAsRead.isSelected = true // Example: use isSelected state
            binding.actionMarkAsRead.setIconResource(R.drawable.ic_check_2)
            // binding.fabMarkAsRead.setImageResource(R.drawable.ic_check_circle) // If you have a filled check
        } else {
            binding.actionMarkAsRead.isSelected = false
            binding.actionMarkAsRead.setIconResource(R.drawable.ic_check)
            // binding.fabMarkAsRead.setImageResource(R.drawable.ic_check) // Default check
        }
    }


    private fun isVerseRead(): Boolean {
        val sharedPreferences = getSharedPreferences("read_verses", Context.MODE_PRIVATE)
        // Ensure verses list is not empty and currentVerseIndex is valid
        if (verses.isEmpty() || currentVerseIndex < 0 || currentVerseIndex >= verses.size) {
            return false
        }
        val verseId = verses[currentVerseIndex].verse_id
        return sharedPreferences.getBoolean("$verseId", false)
    }

    private fun markVerseAsRead() {
        // Ensure verses list is not empty and currentVerseIndex is valid
        if (verses.isEmpty() || currentVerseIndex < 0 || currentVerseIndex >= verses.size) {
            return
        }
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
        // Ensure verses list is not empty and currentVerseIndex is valid
        if (verses.isEmpty() || currentVerseIndex < 0 || currentVerseIndex >= verses.size) {
            return
        }
        val sharedPreferences = getSharedPreferences("read_verses", Context.MODE_PRIVATE)
        val verseId = verses[currentVerseIndex].verse_id
        sharedPreferences.edit().putBoolean("$verseId", false).apply()
    }

    private fun onFavoriteButtonClick() {
        if (verses.isEmpty() || currentVerseIndex < 0 || currentVerseIndex >= verses.size) {
            Toast.makeText(this, "Verse data not available.", Toast.LENGTH_SHORT).show()
            return
        }
        val currentVerse = verses[currentVerseIndex]
        val verseId = currentVerse.verse_id
        val verseText = currentVerse.text // Or construct as needed, e.g., title + content

        val dbHelper = FavoriteDbHelper(this)

        // Check if the verse is already a favorite
        val existingFavorite = dbHelper.getFavoriteByVerseId(verseId)

        if (existingFavorite == null) {
            // Add to favorites
            val chapterId = currentVerse.chapter_number // Get chapter_number
            val result = dbHelper.addFavorite(chapterId, verseId, verseText)
            if (result != -1L) {
                Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show()
                 binding.actionButtonFavorite.setIconResource(R.drawable.ic_star_2) // Update FAB/menu icon if dynamic
            } else {
                Toast.makeText(this, "Failed to add to Favorites", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Remove from favorites
            val result = dbHelper.removeFavorite(verseId)
            if (result > 0) {
                Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show()
                binding.actionButtonFavorite.setIconResource(R.drawable.ic_star) // Update FAB/menu icon if dynamic
            } else {
                Toast.makeText(this, "Failed to remove from Favorites", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Method to update favorite button based on whether the verse is in favorites
     private fun updateFavoriteButtonStatus() {
         if (::verses.isInitialized && verses.isNotEmpty()) {
             val currentVerse = verses[currentVerseIndex]
             val dbHelper = FavoriteDbHelper(this)
             val isFavorite = dbHelper.getFavoriteByVerseId(currentVerse.verse_id) != null
             if (isFavorite) {
                 binding.actionButtonFavorite.setIconResource(R.drawable.ic_star_2)
             } else {
                 binding.actionButtonFavorite.setIconResource(R.drawable.ic_star)
             }
         }
     }

    private fun updateTextSize(newSize: Int) {

        currentTextSize = newSize
        val textViewList = listOf(
            binding.verseTitleTextView,
            binding.verseContentTextView,
            binding.verseTransliterationTextView,
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

        // Removed CustomSpinnerAdapter updates for authorSpinner and cAuthorSpinner

        val audioSourceAdapter = binding.audioSourceDropdown.adapter as? ArrayAdapter<*>
        audioSourceAdapter?.let {
            // Consider if AutoCompleteTextView text size needs dynamic updates here
            // For now, assuming the default text size handling or theme-based size is sufficient
            // binding.audioSourceDropdown.textSize = newSize.toFloat() // This might not be the correct way for AutoCompleteTextView
            // It's usually controlled by the style or theme.
            // If specific text size control is needed for the dropdown items,
            // a custom adapter for AutoCompleteTextView might be required, or styling the dropdown items.
            // For the input field itself, it's part of TextInputLayout and usually follows Material Design guidelines.
            binding.authorAutoCompleteTextView.textSize = newSize.toFloat()
            binding.cAuthorAutoCompleteTextView.textSize = newSize.toFloat()
            (it as ArrayAdapter<String>).notifyDataSetChanged()
        }

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
        updateFabReadStatus() // Ensure FAB updates when verse changes
        // binding.readMRadioBtn.isChecked = isVerseRead() // Handled by FAB now
         updateFavoriteButtonStatus() // Update favorite button when verse changes - Removed

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


    private fun playAudio(audioUrl: String, progressBar: ProgressBar, audioType: AudioUrlHelper.AudioType, chapterNum: Int, verseNum: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val fileName = AudioUrlHelper.getFileNameFromUrl(audioUrl, audioType, chapterNum, verseNum)
            val cacheDir = applicationContext.cacheDir
            val subDirName = "audio_cache"
            // val chapterNumber = verses[currentVerseIndex].chapter_number // Already passed as chapterNum
            val subDir = File(cacheDir, "$subDirName/chapter_$chapterNum")

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
            binding.fabPlay.setIconResource(R.drawable.ic_pause)
        } else {
            binding.fabPlay.setIconResource(R.drawable.ic_play)
        }
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