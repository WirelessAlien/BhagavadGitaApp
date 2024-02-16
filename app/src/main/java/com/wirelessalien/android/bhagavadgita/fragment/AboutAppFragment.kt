/*
 * This file is part of BhagavadGitaApp <https://github.com/WirelessAlien/BhagavadGitaApp>
 * Copyright (C) 2023  WirelessAlien <https://github.com/WirelessAlien>
 *
 * BhagavadGitaApp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BhagavadGitaApp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.wirelessalien.android.bhagavadgita.fragment

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wirelessalien.android.bhagavadgita.BuildConfig
import com.wirelessalien.android.bhagavadgita.databinding.AboutAppFragmentBinding

class AboutAppFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = AboutAppFragmentBinding.inflate(layoutInflater)
        val dialogView = binding.root

        binding.versionNumberText.text = BuildConfig.VERSION_NAME

        binding.githubIcon.setOnClickListener {
            openUrl("https://github.com/WirelessAlien/BhagavadGitaApp")
        }

        binding.githubIssueButton.setOnClickListener {
            openUrl("https://github.com/WirelessAlien/BhagavadGitaApp/issues")
        }

        binding.licenseText.setOnClickListener {
            openUrl("https://www.gnu.org/licenses/gpl-3.0.en.html")
        }

        binding.shareIcon.setOnClickListener {
            val githubUrl = "https://github.com/WirelessAlien/BhagavadGitaApp"
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, githubUrl)
            startActivity(Intent.createChooser(shareIntent, "Share App Link"))
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
