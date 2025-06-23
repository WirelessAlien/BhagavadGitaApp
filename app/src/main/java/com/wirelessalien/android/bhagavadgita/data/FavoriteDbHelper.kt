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

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FavoriteDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "Favorites.db"
        private const val TABLE_FAVORITES = "favorites"
        private const val COLUMN_ID = "id"
        private const val COLUMN_CHAPTER_ID = "chapter_id"
        private const val COLUMN_VERSE_ID = "verse_id"
        private const val COLUMN_VERSE_TITLE = "verse_title"
        private const val COLUMN_VERSE_TEXT = "verse_text"
        private const val COLUMN_USER_NOTE = "user_note"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = "CREATE TABLE $TABLE_FAVORITES (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_CHAPTER_ID INTEGER NOT NULL," +
                "$COLUMN_VERSE_ID INTEGER UNIQUE NOT NULL," +
                "$COLUMN_VERSE_TITLE TEXT NOT NULL," +
                "$COLUMN_VERSE_TEXT TEXT NOT NULL," +
                "$COLUMN_USER_NOTE TEXT)"
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_FAVORITES ADD COLUMN $COLUMN_CHAPTER_ID INTEGER NOT NULL DEFAULT 0;")

        }
    }

    fun addFavorite(chapterId: Int, verseId: Int, verseTitle: String, verseText: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CHAPTER_ID, chapterId)
            put(COLUMN_VERSE_ID, verseId)
            put(COLUMN_VERSE_TITLE, verseTitle)
            put(COLUMN_VERSE_TEXT, verseText)
        }
        val id = db.insert(TABLE_FAVORITES, null, values)
        db.close()
        return id
    }

    fun removeFavorite(verseId: Int): Int {
        val db = this.writableDatabase
        val result = db.delete(TABLE_FAVORITES, "$COLUMN_VERSE_ID = ?", arrayOf(verseId.toString()))
        db.close()
        return result
    }

    fun getAllFavorites(): List<FavoriteVerseEntity> {
        val favoriteList = mutableListOf<FavoriteVerseEntity>()
        val selectQuery = "SELECT * FROM $TABLE_FAVORITES"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val chapterId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CHAPTER_ID))
                val verseId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VERSE_ID))
                val verseTitle = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VERSE_TITLE))
                val verseText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VERSE_TEXT))
                val userNote = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NOTE))
                favoriteList.add(FavoriteVerseEntity(id, chapterId, verseId, verseTitle, verseText, userNote))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return favoriteList
    }

    fun getFavoriteByVerseId(verseId: Int): FavoriteVerseEntity? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_FAVORITES,
            arrayOf(COLUMN_ID, COLUMN_CHAPTER_ID, COLUMN_VERSE_ID, COLUMN_VERSE_TITLE, COLUMN_VERSE_TEXT, COLUMN_USER_NOTE),
            "$COLUMN_VERSE_ID = ?",
            arrayOf(verseId.toString()),
            null, null, null, null
        )
        var favoriteVerse: FavoriteVerseEntity? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val chapterId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CHAPTER_ID))
            val fetchedVerseId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VERSE_ID))
            val verseTitle = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VERSE_TITLE))
            val verseText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VERSE_TEXT))
            val userNote = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NOTE))
            favoriteVerse = FavoriteVerseEntity(id, chapterId, fetchedVerseId, verseTitle, verseText, userNote)
        }
        cursor.close()
        db.close()
        return favoriteVerse
    }

    fun addOrUpdateUserNote(verseId: Int, note: String): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_NOTE, note)
        }
        val result = db.update(
            TABLE_FAVORITES,
            values,
            "$COLUMN_VERSE_ID = ?",
            arrayOf(verseId.toString())
        )
        db.close()
        return result
    }
}
