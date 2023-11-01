package com.wirelessalien.android.bhagavadgita.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContentProviderCompat.requireContext
import com.wirelessalien.android.bhagavadgita.R

class Themes {
    companion object{
        fun loadTheme(context: Context){
            val sharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

            when (sharedPreferences.getInt("themeMode", 0)) {
                0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                2 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    context.setTheme(R.style.AppTheme_Black)
                }
            }
        }
    }
}