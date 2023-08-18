
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
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.color.DynamicColors
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wirelessalien.android.bhagavadgita.activity.AboutGitaActivity
import com.wirelessalien.android.bhagavadgita.activity.HanumanChalisaActivity
import com.wirelessalien.android.bhagavadgita.adapter.ChapterAdapter
import com.wirelessalien.android.bhagavadgita.adapter.SliderVerseAdapter
import com.wirelessalien.android.bhagavadgita.data.Chapter
import com.wirelessalien.android.bhagavadgita.data.Verse
import com.wirelessalien.android.bhagavadgita.databinding.ActivityMainBinding
import com.wirelessalien.android.bhagavadgita.fragment.ThemeFragment
import org.json.JSONArray
import java.io.IOException
import java.nio.charset.Charset
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var chapterList: List<Chapter>
    private lateinit var verseList: List<Verse>
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

        when (sharedPreferences.getString("chosenTheme", "default")) {
            "black" -> setTheme(R.style.AppTheme_Black)
            else -> setTheme(R.style.AppTheme)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        DynamicColors.applyToActivityIfAvailable(this)

        verseList = loadVersesFromJson()
        verseList = verseList.shuffled(Random(System.currentTimeMillis()))

        // Load JSON data from assets
        val jsonString = applicationContext.assets.open("chapters.json").bufferedReader().use {
            it.readText()
        }

        // Parse JSON data
        chapterList = parseJson(jsonString)

        // Setup RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = ChapterAdapter(chapterList)

        // Setup the toolbar
        setSupportActionBar(binding.toolbar)

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

        binding.hanumanChalisaText.setOnClickListener {
            val intent = Intent(this, HanumanChalisaActivity::class.java)
            startActivity(intent)
        }

        binding.btnAboutGita.setOnClickListener {
            val intent = Intent(this, AboutGitaActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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
                val themeDialog = ThemeFragment()
                themeDialog.show(supportFragmentManager, "theme_dialog")
                return true
            }

        }
        return super.onOptionsItemSelected(item)
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