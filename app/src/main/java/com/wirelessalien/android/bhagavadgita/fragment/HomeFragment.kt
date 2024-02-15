
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

package com.wirelessalien.android.bhagavadgita.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.color.DynamicColors
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wirelessalien.android.bhagavadgita.R
import com.wirelessalien.android.bhagavadgita.activity.AboutGitaActivity
import com.wirelessalien.android.bhagavadgita.activity.HanumanChalisaActivity
import com.wirelessalien.android.bhagavadgita.adapter.ChapterAdapter
import com.wirelessalien.android.bhagavadgita.adapter.SliderVerseAdapter
import com.wirelessalien.android.bhagavadgita.data.Chapter
import com.wirelessalien.android.bhagavadgita.data.Verse
import com.wirelessalien.android.bhagavadgita.databinding.FragmentHomeBinding
import com.wirelessalien.android.bhagavadgita.utils.Themes
import org.json.JSONArray
import java.io.IOException
import java.nio.charset.Charset
import kotlin.random.Random


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var chapterList: List<Chapter>
    private lateinit var verseList: List<Verse>
    private lateinit var viewPager: ViewPager2
    private var currentTextSize: Int = 16 // Default text size

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        exitTransition = inflater.inflateTransition(R.transition.slide_right)
        Themes.loadTheme(requireActivity())

        val sharedPrefTextSize = requireActivity().getSharedPreferences("text_size_prefs", Context.MODE_PRIVATE)
        currentTextSize = sharedPrefTextSize.getInt("text_size", 16) // Get the saved text size

        DynamicColors.applyToActivityIfAvailable(requireActivity())

        verseList = loadVersesFromJson()
        verseList = verseList.shuffled(Random(System.currentTimeMillis()))

        // Load JSON data from assets
        val jsonString = requireActivity().applicationContext.assets.open("chapters.json").bufferedReader().use {
            it.readText()
        }

        // Parse JSON data
        chapterList = parseJson(jsonString)

        val adapterC = ChapterAdapter(chapterList, 16)
        binding.recyclerView.adapter = adapterC
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        updateAdapterTextSize(currentTextSize)
        // Setup the toolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)

        // Setup ViewPager
        viewPager = binding.viewPager
        val adapter = SliderVerseAdapter(verseList)
        binding.viewPager.adapter = adapter

        // Auto slide after every 10 seconds
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                binding.viewPager.currentItem = (binding.viewPager.currentItem + 1) % verseList.size
                handler.postDelayed(this, 10000)
            }
        }
        handler.postDelayed(runnable, 10000)

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
            val intent = Intent(requireContext(), HanumanChalisaActivity::class.java)
            startActivity(intent)
        }

        setHasOptionsMenu(true)

    }

    override fun onResume() {
        super.onResume()
        val adapterC = binding.recyclerView.adapter as? ChapterAdapter
        adapterC?.updateProgressData()
        adapterC?.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_about_gita -> {
                val intent = Intent(requireContext(), AboutGitaActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_hanuman_chalisa -> {
                val intent = Intent(requireContext(), HanumanChalisaActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_theme -> {
                val themeDialog = ThemeFragment()
                themeDialog.show(requireActivity().supportFragmentManager, "theme_dialog")
                return true
            }
            R.id.nav_about -> {
                val aboutDialog = AboutAppFragment()
                aboutDialog.show(requireActivity().supportFragmentManager, "AboutAppFragment")

            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateAdapterTextSize(newSize: Int) {

        val recyclerViewC = binding.recyclerView
        val adapterC = recyclerViewC.adapter as? ChapterAdapter
        adapterC?.updateTextSize(newSize)

        val sharedPrefTextSize= requireActivity().getSharedPreferences("text_size_prefs", Context.MODE_PRIVATE)
        sharedPrefTextSize.edit().putInt("text_size", newSize).apply()
    }

    private fun loadVersesFromJson(): List<Verse> {
        val json: String?
        try {
            val inputStream = requireActivity().assets.open("verse.json")
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
