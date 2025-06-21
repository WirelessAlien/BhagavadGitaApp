package com.wirelessalien.android.bhagavadgita.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.wirelessalien.android.bhagavadgita.R

class Themes {
    companion object{
        fun loadTheme(context: Context){
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            when (sharedPreferences.getString("theme_preference", "system")) {
                "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                "black" -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    context.setTheme(R.style.AppTheme_Black)
                }
            }
        }
    }
}