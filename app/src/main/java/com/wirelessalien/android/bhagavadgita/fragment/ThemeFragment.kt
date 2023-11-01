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
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wirelessalien.android.bhagavadgita.MainActivity
import com.wirelessalien.android.bhagavadgita.R
import com.wirelessalien.android.bhagavadgita.databinding.FragmentThemeBinding
import kotlin.properties.Delegates

class ThemeFragment : DialogFragment() {
    private lateinit var binding: FragmentThemeBinding
    private var themeMode: Int = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentThemeBinding.inflate(LayoutInflater.from(requireContext()))
        val view = binding.root

        val sharedPreferences =
            requireContext().getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val sharedPrefEditor = sharedPreferences.edit()

        // Initialize the checked state based on shared preferences
        themeMode = sharedPreferences.getInt("themeMode", 0)
        when (themeMode) {
            0 -> binding.lightThemeButton.isChecked = true
            1 -> binding.darkThemeButton.isChecked = true
            2 -> binding.blackThemeButton.isChecked = true
        }

        if (binding.themeRadioGroup.checkedRadioButtonId == View.NO_ID) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Choose Theme")
            .setView(view)
            .setPositiveButton("OK") { _, _ ->
                if (binding.lightThemeButton.isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    sharedPrefEditor.putInt("themeMode", 0)
                } else if (binding.darkThemeButton.isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    sharedPrefEditor.putInt("themeMode", 1)
                } else if (binding.blackThemeButton.isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    requireActivity().setTheme(R.style.AppTheme_Black)
                    sharedPrefEditor.putInt("themeMode", 2)
                }
                sharedPrefEditor.commit()
                dismiss()
                (requireActivity()).recreate()
            }
            .setNegativeButton("Cancel") { _, _ -> dismiss() }
            .create()
    }
}
