
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
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.wirelessalien.android.bhagavadgita.databinding.ActivityAboutGitaBinding
import com.wirelessalien.android.bhagavadgita.utils.Themes

class AboutGitaActivity: AppCompatActivity() {

    private lateinit var binding: ActivityAboutGitaBinding
    private var currentTextSize: Int = 16

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Themes.loadTheme(this)

        binding = ActivityAboutGitaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPrefTextSize = PreferenceManager.getDefaultSharedPreferences(this)
        currentTextSize = sharedPrefTextSize.getInt("text_size_preference", 16) // Get the saved text size

        updateTextSize(currentTextSize)

    }

    private fun updateTextSize(newSize: Int) {

        currentTextSize = newSize
        val textViewList = listOf(
            binding.textViewaboutGita1,
            binding.textViewaboutGita3

        )

        textViewList.forEach { textView ->
            textView.textSize = newSize.toFloat()
        }

    }
}