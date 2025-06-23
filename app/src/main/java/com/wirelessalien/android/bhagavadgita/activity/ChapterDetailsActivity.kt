
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
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wirelessalien.android.bhagavadgita.R
import com.wirelessalien.android.bhagavadgita.adapter.VerseAdapter
import com.wirelessalien.android.bhagavadgita.data.Verse
import com.wirelessalien.android.bhagavadgita.databinding.ActivityChapterDetailBinding
import com.wirelessalien.android.bhagavadgita.utils.AudioUrlHelper
import com.wirelessalien.android.bhagavadgita.utils.Themes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.Locale

class ChapterDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChapterDetailBinding
    private var verseList: List<Verse> = emptyList()
    private var isSummaryExpanded = false
    private var isSummaryHindiExpanded = false
    private var currentTextSize: Int = 16

    private lateinit var mediaPlayer: MediaPlayer
    private var isChapterPlaying = false
    private var currentTrackIndex = 0
    private lateinit var audioTypes: List<AudioUrlHelper.AudioType>
    private lateinit var selectedAudioType: AudioUrlHelper.AudioType
    private var chapterNumber: Int = 0


    @DelicateCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Themes.loadTheme(this)

        binding = ActivityChapterDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPrefTextSize = PreferenceManager.getDefaultSharedPreferences(this)
        currentTextSize = sharedPrefTextSize.getInt("text_size_preference", 16) // Get the saved text size

        updateAdapterTextSize(currentTextSize)
        updateTextSize(currentTextSize)
        setSupportActionBar(binding.toolbar)

        // Retrieve the chapter details from the intent
        val chapterNumber = intent.getIntExtra("chapter_number", 0)
        val chapterName = intent.getStringExtra("chapter_name")
        val chapterNameMeaning = intent.getStringExtra("name_meaning")
        val chapterSummary = intent.getStringExtra("chapter_summary")
        val chapterSummaryHindi = intent.getStringExtra("chapter_summary_hindi")
        val versesCount = intent.getIntExtra("verses_count", 0)

        binding.progressBar.visibility = View.VISIBLE

        val verse = loadVersesForChapter(chapterNumber)
        val adapter = VerseAdapter(verse, currentTextSize)
        binding.verseRecyclerView.adapter = adapter
        binding.verseRecyclerView.layoutManager = LinearLayoutManager(this)

        // Load the verses asynchronously
        GlobalScope.launch(Dispatchers.IO) {
            verseList = loadVersesForChapter(chapterNumber)

            // Update the UI on the main thread
            withContext(Dispatchers.Main) {
                // Set the chapter details in the UI
                binding.chapterNameTextView.text = chapterName
                binding.chapterNameMeaningTextView.text = chapterNameMeaning
                binding.verseRecyclerView.layoutManager = LinearLayoutManager(this@ChapterDetailsActivity)
                binding.verseRecyclerView.adapter = VerseAdapter(verseList, currentTextSize)

                supportActionBar?.title = "Chapter $chapterNumber"

                // Hide the ProgressBar once the verses are loaded
                binding.progressBar.visibility = View.GONE
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
        binding.verseRecyclerView.adapter = VerseAdapter(verseList, 16)

        // Calculate the number of read verses
        val sharedPreferences = binding.root.context.getSharedPreferences("read_verses", Context.MODE_PRIVATE)
        val readVerses = sharedPreferences.all.keys.count {
            it.endsWith("-chapter") && sharedPreferences.getInt(it, 0) == chapterNumber && sharedPreferences.getBoolean(it.removeSuffix("-chapter"), false)            }

        val progress = (readVerses.toDouble() / versesCount.toDouble()) * 100

        binding.progressBarReadCount.progress = progress.toInt()
        binding.progressTextView.text = String.format(Locale.getDefault(),"%.2f%%", progress)

        mediaPlayer = MediaPlayer()
        audioTypes = AudioUrlHelper.audioOptions

        this.chapterNumber = chapterNumber

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val savedAudioType = sharedPref.getString("selectedAudioType", audioTypes.first().displayName)
        selectedAudioType = audioTypes.find { it.displayName == savedAudioType } ?: audioTypes.first()

        val audioSourceAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            audioTypes.map { it.displayName }
        )
        binding.audioSourceAutoCompleteTextViewChapter.setAdapter(audioSourceAdapter)

        binding.audioSourceAutoCompleteTextViewChapter.setText(selectedAudioType.displayName, false)

        binding.audioSourceAutoCompleteTextViewChapter.setOnItemClickListener { _, _, position, _ ->
            selectedAudioType = audioTypes[position]
            sharedPref.edit().putString("selectedAudioType", selectedAudioType.displayName).apply()
            if (isChapterPlaying) {
                stopChapterAudio()
            }
            currentTrackIndex = 0
        }

        binding.fabPlayPauseChapter.setOnClickListener {
            if (isChapterPlaying) {
                pauseChapterAudio()
            } else {
                playChapterAudio()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val adapter = binding.verseRecyclerView.adapter as? VerseAdapter
        adapter?.updateProgressData()
        adapter?.notifyDataSetChanged()
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

    private fun updateTextSize(newSize: Int) {

        currentTextSize = newSize
        val textViewList = listOf(
            binding.chapterNameTextView,
            binding.chapterNameMeaningTextView,
            binding.chapterSummaryTextView,
            binding.chapterSummaryHindiTextView,
            binding.seeMoreTextView,
            binding.seeMoreHindiTextView
        )

        textViewList.forEach { textView ->
            textView.textSize = newSize.toFloat()
        }
    }

    private fun updateAdapterTextSize(newSize: Int) {

        val recyclerViewC = binding.verseRecyclerView
        val adapterC = recyclerViewC.adapter as? VerseAdapter
        adapterC?.updateTextSize(newSize)
    }

    private fun playChapterAudio() {
        if (verseList.isEmpty()) {
            Toast.makeText(this, "Verses not loaded yet.", Toast.LENGTH_SHORT).show()
            return
        }
        if (currentTrackIndex >= verseList.size) {
            // All tracks played
            stopChapterAudio()
            Toast.makeText(this, "Finished playing chapter.", Toast.LENGTH_SHORT).show()
            return
        }
        playTrack(currentTrackIndex)
    }

    private fun playTrack(trackIndex: Int) {
        if (trackIndex < 0 || trackIndex >= verseList.size) {
            stopChapterAudio()
            return
        }

        val verse = verseList[trackIndex]
        val audioUrl = AudioUrlHelper.getAudioUrl(verse.chapter_number, verse.verse_number, selectedAudioType)

        if (audioUrl == null) {
            Toast.makeText(this, "Audio URL not found for verse ${verse.verse_number}", Toast.LENGTH_SHORT).show()
            currentTrackIndex++
            playChapterAudio()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val fileName = AudioUrlHelper.getFileNameFromUrl(audioUrl, selectedAudioType, verse.chapter_number, verse.verse_number)
            val cacheDir = applicationContext.cacheDir
            val subDirName = "audio_cache"
            val subDir = File(cacheDir, "$subDirName/chapter_${verse.chapter_number}")

            if (!subDir.exists()) {
                subDir.mkdirs()
            }
            val file = File(subDir, fileName)

            withContext(Dispatchers.Main) {
                binding.audioLoadingProgressBarChapter.visibility = View.VISIBLE
                binding.fabPlayPauseChapter.isEnabled = false // Disable while loading
            }

            try {
                if (!file.exists()) {
                    withContext(Dispatchers.IO) {
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
                            // val progress = (totalBytesRead * 100 / contentLength) // Progress for single file
                        }
                        inputStream.close()
                        outputStream.close()
                    }
                }

                withContext(Dispatchers.Main) {
                    mediaPlayer.reset() // Reset before setting new data source
                    mediaPlayer.setDataSource(file.absolutePath)
                    mediaPlayer.prepareAsync() // Use prepareAsync for network/local file streams

                    mediaPlayer.setOnPreparedListener { mp ->
                        binding.audioLoadingProgressBarChapter.visibility = View.GONE
                        binding.fabPlayPauseChapter.isEnabled = true
                        mp.start()
                        isChapterPlaying = true
                        updatePlayPauseChapterButton()
                    }

                    mediaPlayer.setOnCompletionListener {
                        currentTrackIndex++
                        if (currentTrackIndex < verseList.size) {
                            playTrack(currentTrackIndex)
                        } else {
                            stopChapterAudio()
                            Toast.makeText(this@ChapterDetailsActivity, "Finished playing chapter.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    mediaPlayer.setOnErrorListener { mp, what, extra ->
                        binding.audioLoadingProgressBarChapter.visibility = View.GONE
                        binding.fabPlayPauseChapter.isEnabled = true
                        Toast.makeText(this@ChapterDetailsActivity, "Error playing audio.", Toast.LENGTH_SHORT).show()
                        stopChapterAudio() // Stop on error
                        true // Error handled
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    binding.audioLoadingProgressBarChapter.visibility = View.GONE
                    binding.fabPlayPauseChapter.isEnabled = true
                    Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Try next track on error
                    currentTrackIndex++
                    playChapterAudio()
                }
            }
        }
    }

    private fun pauseChapterAudio() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
        isChapterPlaying = false
        updatePlayPauseChapterButton()
    }

    private fun stopChapterAudio() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.reset()
        isChapterPlaying = false
        currentTrackIndex = 0
        updatePlayPauseChapterButton()
        binding.audioLoadingProgressBarChapter.visibility = View.GONE
        binding.fabPlayPauseChapter.isEnabled = true
    }


    private fun updatePlayPauseChapterButton() {
        if (isChapterPlaying) {
            binding.fabPlayPauseChapter.setIconResource(R.drawable.ic_pause)
        } else {
            binding.fabPlayPauseChapter.setIconResource(R.drawable.ic_play)
        }
    }

    override fun onStop() {
        super.onStop()
        if (::mediaPlayer.isInitialized && isChapterPlaying) {
            // Keep playing if activity is just stopped but not destroyed,
            // or pause if that's the desired behavior.
            // For continuous chapter play, typically you might want it to continue if background play is intended.
            // However, for simplicity and standard behavior, we'll pause it.
            pauseChapterAudio()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}

