
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

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.wirelessalien.android.bhagavadgita.R
import com.wirelessalien.android.bhagavadgita.databinding.ActivityHanumanChalisaBinding
import com.wirelessalien.android.bhagavadgita.fragment.HanumanChalisaE
import com.wirelessalien.android.bhagavadgita.fragment.HanumanChalisaH

class HanumanChalisaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHanumanChalisaBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

        when (sharedPreferences.getString("chosenTheme", "default")) {
            "black" -> setTheme(R.style.AppTheme_Black)
            else -> setTheme(R.style.AppTheme)
        }

        binding = ActivityHanumanChalisaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewPager = binding.viewPager
        tabLayout = binding.tabLayout

        val fragmentList = listOf(HanumanChalisaH(), HanumanChalisaE())

        viewPager.adapter = TabPagerAdapter(this, fragmentList)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Hindi"
                1 -> tab.text = "English"
            }
        }.attach()
    }
    private inner class TabPagerAdapter(fragmentActivity: AppCompatActivity, private val fragments: List<Fragment>) :
        FragmentStateAdapter(fragmentActivity) {

        override fun getItemCount(): Int {
            return fragments.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }
}
