
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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.wirelessalien.android.bhagavadgita.databinding.FragmentHanumanChalisaEBinding

class HanumanChalisaE : Fragment() {
    private lateinit var binding: FragmentHanumanChalisaEBinding
    private var currentTextSize: Int = 16

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHanumanChalisaEBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load the saved text size from SharedPreferences
        val sharedPrefTextSize = requireActivity().getSharedPreferences("text_size_prefs", Context.MODE_PRIVATE)
        currentTextSize = sharedPrefTextSize.getInt("text_size", 16)

        // Set the initial text size
        updateTextSize(currentTextSize)

    }

    private fun updateTextSize(newSize: Int) {
        currentTextSize = newSize

        val textViewList = listOf(
            binding.hanumanChalisaE
        )

        textViewList.forEach { textView ->
            textView.textSize = newSize.toFloat()
        }
    }
}