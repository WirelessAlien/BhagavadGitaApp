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
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.wirelessalien.android.bhagavadgita.R
import com.wirelessalien.android.bhagavadgita.databinding.ActivityFragmentBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter

class FragmentActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var binding: ActivityFragmentBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the UncaughtExceptionHandler
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            val crashLog = StringWriter()
            throwable.printStackTrace(PrintWriter(crashLog))

            try {
                val fileName = "BhagavadGitaApp_Crash_Log.txt"
                val targetFile = File(filesDir, fileName)
                FileOutputStream(targetFile)
                    .use { stream ->
                        stream.write(crashLog.toString().toByteArray())
                    }

            } catch (e: IOException) {
                e.printStackTrace()
            }

            android.os.Process.killProcess(android.os.Process.myPid())
        }

        binding = ActivityFragmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        bottomNav = binding.bottomNavI
        bottomNav.setupWithNavController(navController)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    navController.navigate(R.id.homeFragment)
                    true
                }
                R.id.allVerse -> {
                    navController.navigate(R.id.allVerseFragment)
                    true
                }
                R.id.favourite -> {
                    navController.navigate(R.id.favouriteFragment)
                    true
                }
                else -> false
            }
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController.currentDestination?.id != R.id.homeFragment) {
                    navController.navigate(R.id.homeFragment)
                } else {
                    finish()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)
    }
}