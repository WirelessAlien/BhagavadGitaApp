package com.wirelessalien.android.bhagavadgita.data

data class Verse(
    val chapter_id: Int,
    val chapter_number: Int,
    val externalId: Int,
    val id: Int,
    val text: String,
    val title: String,
    val verse_number: Int,
    val verse_order: Int,
    val transliteration: String,
    val word_meanings: String
)
