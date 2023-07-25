package com.wirelessalien.android.bhagavadgita

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationView
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

        // Setup the navigation drawer
        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        val navView: NavigationView = findViewById(R.id.navView)
        val btnHanumanChalisa: Button = findViewById(R.id.hanumanChalisaText)
        val btnAboutGita: Button = findViewById(R.id.btn_about_gita)

        // Create a toggle for the navigation drawer icon
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

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


        // Handle navigation item clicks
        navView.setNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.nav_about_gita -> {
                    intent.setClass(this, AboutGitaActivity::class.java)
                    startActivity(intent)

                }
                R.id.nav_hanuman_chalisa -> {
                    intent.setClass(this, HanumanChalisaActivity::class.java)
                    startActivity(intent)
                }
            }

            // Close the drawer after handling the item click
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
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