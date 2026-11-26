package com.shabydevelop.eattrack

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.DatabaseUtils

class EatingRepository(context: Context) {
    private val db = EatingDatabase(context.applicationContext)

    fun addEvent(healthy: Boolean) {
        val values = ContentValues().apply {
            put("created_at", System.currentTimeMillis())
            put("healthy", if (healthy) 1 else 0)
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
        val days = (0..6).map { offset ->
            val start = (today.clone() as java.util.Calendar).apply { add(java.util.Calendar.DAY_OF_YEAR, -offset) }
            val end = (start.clone() as java.util.Calendar).apply { add(java.util.Calendar.DAY_OF_YEAR, 1) }
            val label = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault()).format(start.time)
            DayCount(label, countRange(start.timeInMillis, end.timeInMillis, false), countRange(start.timeInMillis, end.timeInMillis, true))
        }
        fun since(start: java.util.Calendar, healthy: Boolean = false) = countRange(start.timeInMillis, now.timeInMillis + 1, healthy)
        val history = mutableListOf<EatingEvent>()
        db.readableDatabase.rawQuery("SELECT created_at, healthy FROM unhealthy_eating_events ORDER BY created_at DESC, id DESC", null).use {
            while (it.moveToNext()) history.add(EatingEvent(it.getLong(0), it.getInt(1) == 1))
        }
        val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val weekEnd = (week.clone() as java.util.Calendar).apply { add(java.util.Calendar.DAY_OF_YEAR, 6) }
        val monthEnd = (month.clone() as java.util.Calendar).apply { set(java.util.Calendar.DAY_OF_MONTH, getActualMaximum(java.util.Calendar.DAY_OF_MONTH)) }
        val totalLabel = history.lastOrNull()?.let { format.format(java.util.Date(it.timestamp)) + " – " + format.format(today.time) } ?: "אין דיווחים"
        return EatingReport(since(today), since(week), since(month), count(false), days, history,
            format.format(today.time), format.format(week.time) + " – " + format.format(weekEnd.time),
            format.format(month.time) + " – " + format.format(monthEnd.time), totalLabel, since(today, true), since(week, true), since(month, true), count(true))
    }

    private fun countRange(start: Long, end: Long, healthy: Boolean): Long =
        DatabaseUtils.queryNumEntries(db.readableDatabase, "unhealthy_eating_events", "created_at >= ? AND created_at < ? AND healthy = ?", arrayOf(start.toString(), end.toString(), if (healthy) "1" else "0"))

    fun count(healthy: Boolean): Long = DatabaseUtils.queryNumEntries(db.readableDatabase, "unhealthy_eating_events", "healthy = ?", arrayOf(if (healthy) "1" else "0"))
}

private class EatingDatabase(context: Context) : SQLiteOpenHelper(context, "eattrack.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE unhealthy_eating_events (id INTEGER PRIMARY KEY AUTOINCREMENT, created_at INTEGER NOT NULL, healthy INTEGER NOT NULL DEFAULT 0 CHECK (healthy IN (0,1)))")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion == 1 && newVersion == 2) {
            db.execSQL("ALTER TABLE unhealthy_eating_events ADD COLUMN healthy INTEGER NOT NULL DEFAULT 0 CHECK (healthy IN (0,1))")
        } else throw IllegalStateException("Missing database migration: $oldVersion -> $newVersion")
    }
}
