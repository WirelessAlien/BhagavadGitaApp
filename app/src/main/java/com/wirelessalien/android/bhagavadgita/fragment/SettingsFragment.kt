package com.wirelessalien.android.bhagavadgita.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import com.wirelessalien.android.bhagavadgita.R

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "theme_preference" -> {
                val themePreference = findPreference<ListPreference>(key)
                val themeValue = themePreference?.value ?: "system"
                applyTheme(themeValue)
            }
            "text_size_preference" -> {
                val textSizePreference = findPreference<SeekBarPreference>(key)
                val textSizeValue = textSizePreference?.value ?: 16
                applyTextSize(textSizeValue)
                // We might need to recreate the activity to apply text size changes everywhere
                activity?.recreate()
            }
        }
    }

    private fun applyTheme(themeValue: String) {
        when (themeValue) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "black" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                // For a true black theme, we might need a custom theme style
                // For now, it will be the same as dark theme
                activity?.setTheme(R.style.AppTheme_Black)
            }
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        // Save the theme preference
        val sharedPrefs = preferenceScreen.sharedPreferences
        sharedPrefs?.edit()?.putString("theme_preference", themeValue)?.apply()
        activity?.recreate()
    }

    private fun applyTextSize(textSize: Int) {
        // Save the text size preference
        val sharedPrefs = preferenceScreen.sharedPreferences
        sharedPrefs?.edit()?.putInt("text_size_preference", textSize)?.apply()
        // The actual text size update will be handled by MainActivity reading this preference
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }
}
