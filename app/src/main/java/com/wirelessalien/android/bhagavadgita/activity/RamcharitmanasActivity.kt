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

package com.wirelessalien.android.bhagavadgita.activity

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.wirelessalien.android.bhagavadgita.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.wirelessalien.android.bhagavadgita.adapter.RamcharitmanasAdapter
import com.wirelessalien.android.bhagavadgita.data.RamcharitmanasVerse
import com.wirelessalien.android.bhagavadgita.databinding.ActivityRamcharitmanasBinding
import com.wirelessalien.android.bhagavadgita.databinding.BottomSheetRamcharitmanasNavBinding
import com.wirelessalien.android.bhagavadgita.utils.JsonParserHelper
import com.wirelessalien.android.bhagavadgita.utils.Themes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RamcharitmanasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRamcharitmanasBinding
    private lateinit var ramcharitmanasAdapter: RamcharitmanasAdapter
    private val versesList = mutableListOf<RamcharitmanasVerse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Themes.loadTheme(this) // Apply theme
        binding = ActivityRamcharitmanasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        loadRamcharitmanasData()
        setupFab()
    }

    private fun setupFab() {
        binding.fabNavigate.setOnClickListener {
            showNavigationBottomSheet()
        }
    }

    private fun showNavigationBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val sheetBinding = BottomSheetRamcharitmanasNavBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(sheetBinding.root)

        // Populate Kanda
        val kandas = versesList.map { it.kanda }.distinct().sorted()
        val kandaAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, kandas)
        sheetBinding.kandaAutocompleteTextview.setAdapter(kandaAdapter)

        var selectedKanda: String? = null
        var selectedSarga: Int? = null
        var selectedShloka: Int? = null

        sheetBinding.kandaAutocompleteTextview.setOnItemClickListener { parent, _, position, _ ->
            selectedKanda = parent.getItemAtPosition(position) as String
            selectedSarga = null
            selectedShloka = null
            sheetBinding.sargaAutocompleteTextview.setText("", false)
            sheetBinding.shlokaAutocompleteTextview.setText("", false)
            populateSargaDropdown(sheetBinding, selectedKanda)
        }

        sheetBinding.sargaAutocompleteTextview.setOnItemClickListener { parent, _, position, _ ->
            selectedSarga = parent.getItemAtPosition(position) as Int
            selectedShloka = null
            sheetBinding.shlokaAutocompleteTextview.setText("", false)
            populateShlokaDropdown(sheetBinding, selectedKanda, selectedSarga)
        }

        sheetBinding.shlokaAutocompleteTextview.setOnItemClickListener { parent, _, position, _ ->
            selectedShloka = parent.getItemAtPosition(position) as Int
        }


        sheetBinding.buttonCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        sheetBinding.buttonGo.setOnClickListener {
            if (selectedKanda != null && selectedSarga != null && selectedShloka != null) {
                val position = versesList.indexOfFirst {
                    it.kanda == selectedKanda && it.sarga == selectedSarga && it.shloka == selectedShloka
                }
                if (position != -1) {
                    binding.ramcharitmanasRecyclerView.scrollToPosition(position)
                    // Optionally, add a smooth scroll or highlight for better UX
                    // (binding.ramcharitmanasRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
                    bottomSheetDialog.dismiss()
                } else {
                    Toast.makeText(this, "Selected verse not found.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please select Kanda, Sarga, and Shloka.", Toast.LENGTH_SHORT).show()
            }
        }

        bottomSheetDialog.show()
    }

    private fun populateSargaDropdown(sheetBinding: BottomSheetRamcharitmanasNavBinding, kanda: String?) {
        if (kanda == null) {
            sheetBinding.sargaAutocompleteTextview.setAdapter(null)
            sheetBinding.sargaInputLayout.isEnabled = false
            return
        }
        val sargas = versesList.filter { it.kanda == kanda }
            .map { it.sarga }.distinct().sorted()
        val sargaAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, sargas)
        sheetBinding.sargaAutocompleteTextview.setAdapter(sargaAdapter)
        sheetBinding.sargaInputLayout.isEnabled = sargas.isNotEmpty()
    }

    private fun populateShlokaDropdown(sheetBinding: BottomSheetRamcharitmanasNavBinding, kanda: String?, sarga: Int?) {
        if (kanda == null || sarga == null) {
            sheetBinding.shlokaAutocompleteTextview.setAdapter(null)
            sheetBinding.shlokaInputLayout.isEnabled = false
            return
        }
        val shlokas = versesList.filter { it.kanda == kanda && it.sarga == sarga }
            .map { it.shloka }.distinct().sorted()
        val shlokaAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, shlokas)
        sheetBinding.shlokaAutocompleteTextview.setAdapter(shlokaAdapter)
        sheetBinding.shlokaInputLayout.isEnabled = shlokas.isNotEmpty()
    }


    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.ramcharitmanas) // Ensure this string exists
    }

    private fun setupRecyclerView() {
        ramcharitmanasAdapter = RamcharitmanasAdapter(versesList)
        binding.ramcharitmanasRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@RamcharitmanasActivity)
            adapter = ramcharitmanasAdapter
        }
    }

    private fun loadRamcharitmanasData() {
        binding.progressBarRamcharitmanas.visibility = View.VISIBLE
        lifecycleScope.launch {
            val loadedVerses = withContext(Dispatchers.IO) {
                try {
                    JsonParserHelper.parseRamcharitmanasJson(applicationContext, "Ramayan.json")
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Return empty list or handle error appropriately
                    emptyList<RamcharitmanasVerse>()
                }
            }

            binding.progressBarRamcharitmanas.visibility = View.GONE
            if (loadedVerses.isNotEmpty()) {
                binding.ramcharitmanasRecyclerView.visibility = View.VISIBLE
                binding.textViewEmptyState.visibility = View.GONE
                versesList.clear()
                versesList.addAll(loadedVerses)
                ramcharitmanasAdapter.updateData(loadedVerses) // Or notifyItemRangeInserted
            } else {
                binding.ramcharitmanasRecyclerView.visibility = View.GONE
                binding.textViewEmptyState.visibility = View.VISIBLE
                Toast.makeText(this@RamcharitmanasActivity, "Failed to load Ramcharitmanas data.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}