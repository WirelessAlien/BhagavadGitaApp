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

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wirelessalien.android.bhagavadgita.R
import com.wirelessalien.android.bhagavadgita.databinding.FragmentThemeBinding

class ThemeFragment : DialogFragment() {
    private lateinit var binding: FragmentThemeBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentThemeBinding.inflate(LayoutInflater.from(requireContext()))
        val view = binding.root

        val sharedPreferences = requireContext().getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val sharedPrefEditor = sharedPreferences.edit()

        // Initialize the checked state based on shared preferences
        val isDarkMode = sharedPreferences.getBoolean("darkMode", false)
        if (isDarkMode) {
            binding.darkThemeButton.isChecked = true
        } else {
            binding.lightThemeButton.isChecked = true
        }

        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.lightThemeButton -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    sharedPrefEditor.putBoolean("darkMode", false)
                }
                R.id.darkThemeButton -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    sharedPrefEditor.putBoolean("darkMode", true)
                    sharedPrefEditor.putString("chosenTheme", "dark")
                }
                R.id.blackThemeButton -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    sharedPrefEditor.putBoolean("darkMode", true)
                    sharedPrefEditor.putString("chosenTheme", "black")
                }
            }
            sharedPrefEditor.apply()
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Choose Theme")
            .setView(view)
            .setPositiveButton("OK", null)
            .setNegativeButton("Cancel") { _, _ -> dismiss() }
            .create()
    }
}
