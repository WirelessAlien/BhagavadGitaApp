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

object AudioUrlHelper {

    data class AudioType(val displayName: String, val typeCode: String)

    val audioOptions = listOf(
        AudioType("Verse", "verse"),
        AudioType("Translation (English)", "translation_en"),
        AudioType("Translation (Hindi)", "translation_hi")
    )

    fun getAudioUrl(chapterNumber: Int, verseNumber: Int, audioType: AudioType): String? {
        return when (audioType.typeCode) {
            "verse" -> {
                // Format: https://www.gitasupersite.iitk.ac.in/sites/default/files/audio/CHAP1/1-1.MP3
                // Chapter number might need padding if it's single digit, but the example shows CHAP1 for chapter 1.
                // Verse number also seems to be part of a combined string like "1-1".
                // The existing code uses: "https://github.com/WirelessAlien/gita/raw/main/data/verse_recitation/${verses[currentVerseIndex].chapter_number}/${verses[currentVerseIndex].verse_number}.mp3"
                // For now, I'll stick to the new requirement's URL structure.
                "https://www.gitasupersite.iitk.ac.in/sites/default/files/audio/CHAP$chapterNumber/$chapterNumber-$verseNumber.MP3"
            }
            "translation_en" -> {
                // Format: https://www.gitasupersite.iitk.ac.in/sites/default/files/audio/Purohit/1.1.mp3
                // This seems to be chapter.verse
                "https://www.gitasupersite.iitk.ac.in/sites/default/files/audio/Purohit/$chapterNumber.$verseNumber.mp3"
            }
            "translation_hi" -> {
                // Format: https://www.gitasupersite.iitk.ac.in/sites/default/files/audio/Tejomayananda/chapter/C1-H-01.mp3
                // Verse number needs padding (e.g., 01)
                val paddedVerseNumber = String.format("%02d", verseNumber)
                "https://www.gitasupersite.iitk.ac.in/sites/default/files/audio/Tejomayananda/chapter/C$chapterNumber-H-$paddedVerseNumber.mp3"
            }
            else -> null
        }
    }

    fun getFileNameFromUrl(audioUrl: String, audioType: AudioType, chapterNumber: Int, verseNumber: Int): String {
        // Generate a more structured filename for caching to avoid conflicts if URLs are too similar
        // e.g., type_chap_verse.mp3
        return "${audioType.typeCode}_${chapterNumber}_${verseNumber}.mp3"
    }
}
