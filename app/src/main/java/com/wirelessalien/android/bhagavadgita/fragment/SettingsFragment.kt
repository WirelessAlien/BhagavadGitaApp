package com.wirelessalien.android.bhagavadgita.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import com.wirelessalien.android.bhagavadgita.MainActivity
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
            }
        }
    }

    private fun applyTheme(themeValue: String) {
        when (themeValue) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "black" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                activity?.setTheme(R.style.AppTheme_Black)
            }
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        val sharedPrefs = preferenceScreen.sharedPreferences
        sharedPrefs?.edit()?.putString("theme_preference", themeValue)?.apply()
        requireActivity().finish()
        requireActivity().startActivity(Intent(requireContext(), MainActivity::class.java))
    }

    private fun applyTextSize(textSize: Int) {
        val sharedPrefs = preferenceScreen.sharedPreferences
        sharedPrefs?.edit()?.putInt("text_size_preference", textSize)?.apply()

        val sampleTextView = activity?.findViewById<TextView>(R.id.sample_text)
        sampleTextView?.textSize = textSize.toFloat()
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }
}
