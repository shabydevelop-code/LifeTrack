package com.shabydevelop.androiddemo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.DatabaseUtils

class EatingRepository(context: Context) {
    private val db = EatingDatabase(context.applicationContext)

    fun addEvent() {
        val values = ContentValues().apply {
            put("created_at", System.currentTimeMillis())
        }
        db.writableDatabase.insertOrThrow("unhealthy_eating_events", null, values)
    }

    fun reset() {
        db.writableDatabase.delete("unhealthy_eating_events", null, null)
    }

    fun count(): Long = DatabaseUtils.queryNumEntries(db.readableDatabase, "unhealthy_eating_events")
}

private class EatingDatabase(context: Context) : SQLiteOpenHelper(context, "eating.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE unhealthy_eating_events (id INTEGER PRIMARY KEY AUTOINCREMENT, created_at INTEGER NOT NULL)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        throw IllegalStateException("Missing database migration: $oldVersion -> $newVersion")
    }
}
