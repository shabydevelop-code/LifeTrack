package com.shabydevelop.eattrack

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.DatabaseUtils

class EatingRepository(context: Context) {
    private val db = EatingDatabase(context.applicationContext)

    fun addEvent(type: EventType): Long {
        val values = ContentValues().apply {
            put("created_at", System.currentTimeMillis())
            put("event_type", type.name)
        }
        return db.writableDatabase.insertOrThrow("unhealthy_eating_events", null, values)
    }

    fun saveReasons(id: Long, reasons: Set<EatingReason>, type: EventType) {
        val values = ContentValues().apply { put("reasons", reasons.sortedBy { it.ordinal }.joinToString(",") { it.name }) }
        if (db.writableDatabase.update("unhealthy_eating_events", values, "id = ? AND event_type = ?", arrayOf(id.toString(), type.name)) != 1)
            throw android.database.SQLException("Report not found")
    }

    fun deleteEvent(id: Long): Boolean =
        db.writableDatabase.delete("unhealthy_eating_events", "id = ?", arrayOf(id.toString())) == 1

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
            DayCount(label, countRange(start.timeInMillis, end.timeInMillis, EventType.UNHEALTHY), countRange(start.timeInMillis, end.timeInMillis, EventType.HEALTHY), countRange(start.timeInMillis, end.timeInMillis, EventType.AVOIDED))
        }
        fun since(start: java.util.Calendar, type: EventType = EventType.UNHEALTHY) = countRange(start.timeInMillis, now.timeInMillis + 1, type)
        val first = db.readableDatabase.rawQuery("SELECT MIN(created_at) FROM unhealthy_eating_events", null).use {
            if (it.moveToFirst() && !it.isNull(0)) it.getLong(0) else null
        }
        val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val weekEnd = (week.clone() as java.util.Calendar).apply { add(java.util.Calendar.DAY_OF_YEAR, 6) }
        val monthEnd = (month.clone() as java.util.Calendar).apply { set(java.util.Calendar.DAY_OF_MONTH, getActualMaximum(java.util.Calendar.DAY_OF_MONTH)) }
        val totalLabel = first?.let { format.format(java.util.Date(it)) + " – " + format.format(today.time) } ?: "אין דיווחים"
        return EatingReport(since(today), since(week), since(month), count(EventType.UNHEALTHY), days,
            format.format(today.time), format.format(week.time) + " – " + format.format(weekEnd.time),
            format.format(month.time) + " – " + format.format(monthEnd.time), totalLabel, since(today, EventType.HEALTHY), since(week, EventType.HEALTHY), since(month, EventType.HEALTHY), count(EventType.HEALTHY), since(today, EventType.AVOIDED), since(week, EventType.AVOIDED), since(month, EventType.AVOIDED), count(EventType.AVOIDED))
    }

    fun historyPage(days: Int?, type: EventType?, before: EatingEvent?): HistoryPage {
        val clauses = mutableListOf<String>()
        val args = mutableListOf<String>()
        if (days != null) {
            val start = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
                add(java.util.Calendar.DAY_OF_YEAR, 1 - days)
            }
            clauses.add("created_at >= ?"); args.add(start.timeInMillis.toString())
        }
        if (type != null) { clauses.add("event_type = ?"); args.add(type.name) }
        if (before != null) {
            clauses.add("(created_at < ? OR (created_at = ? AND id < ?))")
            args.add(before.timestamp.toString()); args.add(before.timestamp.toString()); args.add(before.id.toString())
        }
        val rows = mutableListOf<EatingEvent>()
        db.readableDatabase.query("unhealthy_eating_events", arrayOf("id", "created_at", "event_type", "reasons"),
            clauses.takeIf { it.isNotEmpty() }?.joinToString(" AND "), args.toTypedArray(), null, null,
            "created_at DESC, id DESC", "31").use {
            while (it.moveToNext()) rows.add(EatingEvent(it.getLong(0), it.getLong(1), EventType.valueOf(it.getString(2)), it.getString(3).split(',').mapNotNull { code -> EatingReason.entries.firstOrNull { reason -> reason.name == code } }))
        }
        return HistoryPage(rows.take(30), rows.size > 30)
    }

    private fun countRange(start: Long, end: Long, type: EventType): Long =
        DatabaseUtils.queryNumEntries(db.readableDatabase, "unhealthy_eating_events", "created_at >= ? AND created_at < ? AND event_type = ?", arrayOf(start.toString(), end.toString(), type.name))

    fun count(type: EventType): Long = DatabaseUtils.queryNumEntries(db.readableDatabase, "unhealthy_eating_events", "event_type = ?", arrayOf(type.name))
}

private class EatingDatabase(context: Context) : SQLiteOpenHelper(context, "eattrack.db", null, 5) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE unhealthy_eating_events (id INTEGER PRIMARY KEY AUTOINCREMENT, created_at INTEGER NOT NULL, event_type TEXT NOT NULL CHECK (event_type IN ('UNHEALTHY','HEALTHY','AVOIDED')), reasons TEXT NOT NULL DEFAULT '')")
        createIndex(db)
    }

    private fun createIndex(db: SQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_eating_time ON unhealthy_eating_events(created_at DESC, id DESC)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion == 1) {
            db.execSQL("ALTER TABLE unhealthy_eating_events ADD COLUMN healthy INTEGER NOT NULL DEFAULT 0 CHECK (healthy IN (0,1))")
        }
        if (oldVersion < 3) createIndex(db)
        if (oldVersion < 4) db.execSQL("ALTER TABLE unhealthy_eating_events ADD COLUMN reasons TEXT NOT NULL DEFAULT ''")
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE unhealthy_eating_events ADD COLUMN event_type TEXT NOT NULL DEFAULT 'UNHEALTHY' CHECK (event_type IN ('UNHEALTHY','HEALTHY','AVOIDED'))")
            db.execSQL("UPDATE unhealthy_eating_events SET event_type = 'HEALTHY' WHERE healthy = 1")
        }
    }
}
