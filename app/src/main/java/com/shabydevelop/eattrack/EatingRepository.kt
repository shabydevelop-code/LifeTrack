package com.shabydevelop.eattrack

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

    fun undoLast(): Boolean {
        return db.writableDatabase.delete("unhealthy_eating_events",
            "id = (SELECT id FROM unhealthy_eating_events ORDER BY id DESC LIMIT 1)", null) > 0
    }

    fun reset() {
        db.writableDatabase.delete("unhealthy_eating_events", null, null)
    }

    fun report(): EatingReport {
        val now = java.util.Calendar.getInstance()
        val today = (now.clone() as java.util.Calendar).apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
        }
        val week = (today.clone() as java.util.Calendar).apply {
            add(java.util.Calendar.DAY_OF_YEAR, -(get(java.util.Calendar.DAY_OF_WEEK) - java.util.Calendar.SUNDAY))
        }
        val month = (today.clone() as java.util.Calendar).apply { set(java.util.Calendar.DAY_OF_MONTH, 1) }
        val days = (6 downTo 0).map { offset ->
            val start = (today.clone() as java.util.Calendar).apply { add(java.util.Calendar.DAY_OF_YEAR, -offset) }
            val end = (start.clone() as java.util.Calendar).apply { add(java.util.Calendar.DAY_OF_YEAR, 1) }
            val label = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault()).format(start.time)
            DayCount(label, DatabaseUtils.queryNumEntries(db.readableDatabase, "unhealthy_eating_events",
                "created_at >= ? AND created_at < ?", arrayOf(start.timeInMillis.toString(), end.timeInMillis.toString())))
        }
        fun since(start: java.util.Calendar) = DatabaseUtils.queryNumEntries(db.readableDatabase,
            "unhealthy_eating_events", "created_at >= ? AND created_at <= ?", arrayOf(start.timeInMillis.toString(), now.timeInMillis.toString()))
        val history = mutableListOf<Long>()
        db.readableDatabase.rawQuery("SELECT created_at FROM unhealthy_eating_events ORDER BY created_at DESC, id DESC", null).use {
            while (it.moveToNext()) history.add(it.getLong(0))
        }
        val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val weekEnd = (week.clone() as java.util.Calendar).apply { add(java.util.Calendar.DAY_OF_YEAR, 6) }
        val monthEnd = (month.clone() as java.util.Calendar).apply { set(java.util.Calendar.DAY_OF_MONTH, getActualMaximum(java.util.Calendar.DAY_OF_MONTH)) }
        val totalLabel = history.lastOrNull()?.let { format.format(java.util.Date(it)) + " – " + format.format(today.time) } ?: "אין דיווחים"
        return EatingReport(since(today), since(week), since(month), count(), days, history,
            format.format(today.time), format.format(week.time) + " – " + format.format(weekEnd.time),
            format.format(month.time) + " – " + format.format(monthEnd.time), totalLabel)
    }

    fun count(): Long = DatabaseUtils.queryNumEntries(db.readableDatabase, "unhealthy_eating_events")
}

private class EatingDatabase(context: Context) : SQLiteOpenHelper(context, "eattrack.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE unhealthy_eating_events (id INTEGER PRIMARY KEY AUTOINCREMENT, created_at INTEGER NOT NULL)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        throw IllegalStateException("Missing database migration: $oldVersion -> $newVersion")
    }
}
