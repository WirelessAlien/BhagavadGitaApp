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

package com.wirelessalien.android.bhagavadgita.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.wirelessalien.android.bhagavadgita.data.RamayanVerse
import java.io.IOException
import java.io.InputStreamReader

object JsonParserHelper {

    fun parseRamcharitmanasJson(context: Context, fileName: String): List<RamayanVerse> {
        val verses = mutableListOf<RamayanVerse>()
        val gson = Gson()

        try {
            context.assets.open(fileName).use { inputStream ->
                InputStreamReader(inputStream, "UTF-8").use { reader ->
                    JsonReader(reader).use { jsonReader ->
                        jsonReader.beginArray()
                        while (jsonReader.hasNext()) {
                            val verse = gson.fromJson<RamayanVerse>(jsonReader, RamayanVerse::class.java)
                            verses.add(verse)
                        }
                        jsonReader.endArray()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle IO exception, perhaps return empty list or throw a custom exception
        } catch (e: com.google.gson.JsonSyntaxException) {
            e.printStackTrace()
            // Handle JSON syntax errors
        }
        return verses
    }
}
