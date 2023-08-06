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

package com.wirelessalien.android.bhagavadgita

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wirelessalien.android.bhagavadgita.activity.AboutGitaActivity
import com.wirelessalien.android.bhagavadgita.activity.HanumanChalisaActivity
import com.wirelessalien.android.bhagavadgita.adapter.ChapterAdapter
import com.wirelessalien.android.bhagavadgita.adapter.SliderVerseAdapter
import com.wirelessalien.android.bhagavadgita.data.Chapter
import com.wirelessalien.android.bhagavadgita.data.Verse
import org.json.JSONArray
import java.io.IOException
import java.nio.charset.Charset
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var chapterList: List<Chapter>
    private lateinit var recyclerView: RecyclerView

    private lateinit var viewPager: ViewPager2
    private lateinit var verseList: List<Verse>


    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.recyclerView)
        viewPager = findViewById(R.id.viewPager)
        verseList = loadVersesFromJson()

        verseList = verseList.shuffled(Random(System.currentTimeMillis()))


        // Load JSON data from assets
        val jsonString = applicationContext.assets.open("chapters.json").bufferedReader().use {
            it.readText()
        }

        // Parse JSON data
        chapterList = parseJson(jsonString)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ChapterAdapter(chapterList)

        // Setup the toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val btnHanumanChalisa: Button = findViewById(R.id.hanumanChalisaText)
        val btnAboutGita: Button = findViewById(R.id.btn_about_gita)


        val adapter = SliderVerseAdapter(verseList)
        viewPager.adapter = adapter

        // Auto slide after every 10 seconds
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                viewPager.currentItem = (viewPager.currentItem + 1) % verseList.size
                handler.postDelayed(this, 10000)
            }
        }

        handler.postDelayed(runnable, 10000)

        btnHanumanChalisa.setOnClickListener {
            // Open the new activity when the button is clicked
            val intent = Intent(this, HanumanChalisaActivity::class.java)
            startActivity(intent)
        }
        btnAboutGita.setOnClickListener {
            // Open the new activity when the button is clicked
            val intent = Intent(this, AboutGitaActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
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