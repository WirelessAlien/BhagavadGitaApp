package com.wirelessalien.android.bhagavadgita.data

data class Chapter(
    val chapter_number: Int,
    val chapter_summary: String,
    val chapter_summary_hindi: String,
    val id: Int,
    val image_name: String,
    val name: String,
    val name_meaning: String,
    val name_translation: String,
    val name_transliterated: String,
    val verses_count: Int
)
