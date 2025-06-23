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

package com.wirelessalien.android.bhagavadgita.data

import com.google.gson.annotations.SerializedName

data class RamayanVerse(
    @SerializedName("kanda")
    val kanda: String,

    @SerializedName("sarga")
    val sarga: Int,

    @SerializedName("shloka")
    val shloka: Int,

    @SerializedName("shloka_text")
    val shlokaText: String,

    @SerializedName("transliteration")
    val transliteration: String?,

    @SerializedName("translation")
    val translation: String?, // Made translation nullable as well, just in case some entries might miss it

    @SerializedName("explanation")
    val explanation: String?, // Made explanation nullable

    @SerializedName("comments")
    val comments: String?,

    var showKanda: Boolean = true,
    var showShlokaText: Boolean = true,
    var showTranslation: Boolean = true,
    var showExplanation: Boolean = true
)
