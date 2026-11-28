package com.shabydevelop.eattrack

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.DatabaseUtils

class EatingRepository(context: Context) : EatingDataSource {
    private val db = EatingDatabase(context.applicationContext)

    override fun addEvent(type: EventType, activity: ActivityKind?, minutes: Int, milliliters: Int): Long {
        val values = ContentValues().apply {
            put("created_at", PlatformClock.nowMillis())
            put("event_type", type.name)
            if (type.isFood) put("healthy_percent", if (type == EventType.HEALTHY) 100 else 0)
            put("activity_kind", activity?.name ?: "")
            put("minutes", minutes)
            put("milliliters", milliliters)
        }
        return db.writableDatabase.insertOrThrow("unhealthy_eating_events", null, values)
    }

    override fun addFood(kind: FoodKind, percent: Int): Long {
        require(percent in 0..100)
        val values = ContentValues().apply {
            put("created_at", PlatformClock.nowMillis())
            put("event_type", if (percent == 100) "HEALTHY" else "UNHEALTHY")
            put("food_kind", kind.name)
            put("healthy_percent", percent)
        }
        return db.writableDatabase.insertOrThrow("unhealthy_eating_events", null, values)
    }

    override fun saveReasons(id: Long, reasons: Set<EatingReason>, type: EventType) {
        val values = ContentValues().apply { put("reasons", reasons.sortedBy { it.ordinal }.joinToString(",") { it.name }) }
        if (db.writableDatabase.update("unhealthy_eating_events", values, "id = ? AND event_type = ?", arrayOf(id.toString(), type.name)) != 1)
            throw android.database.SQLException("Report not found")
    }

    override fun updateEvent(event: EatingEvent) {
        val aerobic = event.type == EventType.AEROBIC
        require(!aerobic || (event.activity != null && event.minutes > 0))
        val values = ContentValues().apply {
            put("created_at", event.timestamp)
            put("event_type", event.type.name)
            put("food_kind", if (event.type.isFood) event.foodKind?.name ?: "" else "")
            if (event.type.isFood) put("healthy_percent", event.healthyPercent ?: if (event.type == EventType.HEALTHY) 100 else 0) else putNull("healthy_percent")
            put("reasons", if (event.type.isFood || event.type == EventType.AVOIDED) event.reasons.joinToString(",") { it.name } else "")
            put("activity_kind", if (aerobic) event.activity!!.name else "")
            put("minutes", if (aerobic) event.minutes else 0)
            put("milliliters", if (event.type == EventType.WATER) event.milliliters else 0)
        }
        if (db.writableDatabase.update("unhealthy_eating_events", values, "id = ?", arrayOf(event.id.toString())) != 1)
            throw android.database.SQLException("Report not found")
    }

    override fun deleteEvent(id: Long): Boolean =
        db.writableDatabase.delete("unhealthy_eating_events", "id = ?", arrayOf(id.toString())) == 1

    override fun undoLast(): Boolean {
        return db.writableDatabase.delete("unhealthy_eating_events",
            "id = (SELECT id FROM unhealthy_eating_events ORDER BY id DESC LIMIT 1)", null) > 0
    }

    override fun reset() {
        db.writableDatabase.delete("unhealthy_eating_events", null, null)
    }

    override fun report(): EatingReport = buildEatingReport(
        PlatformReportCalendar.snapshot(), countRange = ::countRange, count = ::count,
        first = db.readableDatabase.rawQuery("SELECT MIN(created_at) FROM unhealthy_eating_events WHERE event_type IN ('HEALTHY','UNHEALTHY','AVOIDED')", null).use {
            if (it.moveToFirst() && !it.isNull(0)) it.getLong(0) else null
        }, formatDate = PlatformDateTime::date, activityStats = ::activityStats, waterStats = ::waterStats, foodStats = ::foodStats, activityDurations = ::activityDurations
    )

    private fun foodStats(start: Long, end: Long): FoodStats {
        val groups = mutableMapOf<String, FoodGroup>()
        db.readableDatabase.rawQuery("SELECT food_kind, COUNT(*), SUM(COALESCE(healthy_percent, CASE event_type WHEN 'HEALTHY' THEN 100 ELSE 0 END)), SUM(CASE WHEN COALESCE(healthy_percent, CASE event_type WHEN 'HEALTHY' THEN 100 ELSE 0 END) >= 50 THEN 1 ELSE 0 END) FROM unhealthy_eating_events WHERE event_type IN ('HEALTHY','UNHEALTHY') AND created_at >= ? AND created_at < ? GROUP BY food_kind", arrayOf(start.toString(), end.toString())).use {
            while (it.moveToNext()) groups[it.getString(0)] = FoodGroup(it.getLong(1), it.getLong(2), it.getLong(3))
        }
        return FoodStats(groups["MEAL"] ?: FoodGroup(), groups["SNACK"] ?: FoodGroup(), groups[""] ?: FoodGroup())
    }

    private fun waterStats(start: Long, end: Long = PlatformClock.nowMillis() + 1): WaterStats =
        db.readableDatabase.rawQuery("SELECT COUNT(*), COALESCE(SUM(milliliters),0) FROM unhealthy_eating_events WHERE event_type = 'WATER' AND created_at >= ? AND created_at < ?", arrayOf(start.toString(), end.toString())).use { it.moveToFirst(); WaterStats(it.getLong(0), it.getLong(1)) }

    private fun activityStats(start: Long, end: Long = PlatformClock.nowMillis() + 1): ActivityStats =
        db.readableDatabase.rawQuery("SELECT COUNT(*), COALESCE(SUM(minutes),0) FROM unhealthy_eating_events WHERE event_type = 'AEROBIC' AND created_at >= ? AND created_at < ?", arrayOf(start.toString(), end.toString())).use { it.moveToFirst(); ActivityStats(it.getLong(0), it.getLong(1)) }

    private fun activityDurations(start: Long, end: Long): List<ActivityDuration> {
        val totals = mutableMapOf<ActivityKind, Long>()
        db.readableDatabase.rawQuery("SELECT activity_kind, SUM(minutes) FROM unhealthy_eating_events WHERE event_type = 'AEROBIC' AND created_at >= ? AND created_at < ? GROUP BY activity_kind", arrayOf(start.toString(), end.toString())).use { cursor ->
            while (cursor.moveToNext()) {
                val kind = ActivityKind.entries.firstOrNull { it.name == cursor.getString(0) } ?: ActivityKind.OTHER
                totals[kind] = (totals[kind] ?: 0L) + cursor.getLong(1)
            }
        }
        return ActivityKind.entries.mapNotNull { kind -> totals[kind]?.let { ActivityDuration(kind, it) } }
    }

    override fun historyPage(days: Int?, type: EventType?, before: EatingEvent?): HistoryPage =
        queryHistory(days, type, before, false, null)

    override fun foodHistoryPage(days: Int?, kind: FoodKind?, before: EatingEvent?): HistoryPage =
        queryHistory(days, null, before, true, kind)

    private fun queryHistory(days: Int?, type: EventType?, before: EatingEvent?, foodOnly: Boolean, kind: FoodKind?): HistoryPage {
        val clauses = mutableListOf<String>()
        val args = mutableListOf<String>()
        if (days != null) {
            val start = PlatformDateTime.startOfDays(days)
            clauses.add("created_at >= ?"); args.add(start.toString())
        }
        if (type != null) { clauses.add("event_type = ?"); args.add(type.name) }
        if (foodOnly) {
            clauses.add("event_type IN ('HEALTHY','UNHEALTHY') AND food_kind = ?")
            args.add(kind?.name ?: "")
        }
        if (before != null) {
            clauses.add("(created_at > ? OR (created_at = ? AND id > ?))")
            args.add(before.timestamp.toString()); args.add(before.timestamp.toString()); args.add(before.id.toString())
        }
        val rows = mutableListOf<EatingEvent>()
        db.readableDatabase.query("unhealthy_eating_events", arrayOf("id", "created_at", "event_type", "reasons", "activity_kind", "minutes", "milliliters", "food_kind", "healthy_percent"),
            clauses.takeIf { it.isNotEmpty() }?.joinToString(" AND "), args.toTypedArray(), null, null,
            "created_at ASC, id ASC", "31").use {
            while (it.moveToNext()) rows.add(EatingEvent(it.getLong(0), it.getLong(1), EventType.valueOf(it.getString(2)), it.getString(3).split(',').mapNotNull { code -> EatingReason.entries.firstOrNull { reason -> reason.name == code } }, ActivityKind.entries.firstOrNull { kind -> kind.name == it.getString(4) }, it.getInt(5), it.getInt(6), FoodKind.entries.firstOrNull { kind -> kind.name == it.getString(7) }, if (it.isNull(8)) null else it.getInt(8)))
        }
        return HistoryPage(rows.take(30), rows.size > 30)
    }

    private fun countRange(start: Long, end: Long, type: EventType): Long =
        DatabaseUtils.queryNumEntries(db.readableDatabase, "unhealthy_eating_events", "created_at >= ? AND created_at < ? AND event_type = ?", arrayOf(start.toString(), end.toString(), type.name))

    override fun count(type: EventType): Long = DatabaseUtils.queryNumEntries(db.readableDatabase, "unhealthy_eating_events", "event_type = ?", arrayOf(type.name))
}

internal class EatingDatabase(context: Context) : SQLiteOpenHelper(context, "eattrack.db", null, 10) {
    override fun onCreate(db: SQLiteDatabase) {
        createEvents(db)
        addFoodColumns(db)
        createIndex(db)
        createWeights(db)
        createWeightIndex(db)
    }

    private fun createEvents(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE unhealthy_eating_events (id INTEGER PRIMARY KEY AUTOINCREMENT, created_at INTEGER NOT NULL, event_type TEXT NOT NULL CHECK (event_type IN ('UNHEALTHY','HEALTHY','AVOIDED','AEROBIC','WATER')), reasons TEXT NOT NULL DEFAULT '', activity_kind TEXT NOT NULL DEFAULT '', minutes INTEGER NOT NULL DEFAULT 0, milliliters INTEGER NOT NULL DEFAULT 0, CHECK ((event_type = 'WATER' AND milliliters > 0) OR (event_type != 'WATER' AND milliliters = 0)), CHECK ((event_type = 'AEROBIC' AND minutes > 0 AND activity_kind != '') OR (event_type != 'AEROBIC' AND minutes = 0 AND activity_kind = '')))")
    }

    private fun addFoodColumns(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE unhealthy_eating_events ADD COLUMN food_kind TEXT NOT NULL DEFAULT '' CHECK (food_kind IN ('','MEAL','SNACK'))")
        db.execSQL("ALTER TABLE unhealthy_eating_events ADD COLUMN healthy_percent INTEGER CHECK (healthy_percent BETWEEN 0 AND 100)")
        db.execSQL("UPDATE unhealthy_eating_events SET healthy_percent = CASE event_type WHEN 'HEALTHY' THEN 100 WHEN 'UNHEALTHY' THEN 0 ELSE NULL END")
    }

    private fun createIndex(db: SQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_eating_time ON unhealthy_eating_events(created_at DESC, id DESC)")
    }

    private fun createWeights(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS weight_entries (id INTEGER PRIMARY KEY AUTOINCREMENT, created_at INTEGER NOT NULL, grams INTEGER NOT NULL CHECK (grams > 0))")
    }

    private fun createWeightIndex(db: SQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_weight_time ON weight_entries(created_at DESC, id DESC)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 8) createWeights(db)
        if (oldVersion < 9) createWeightIndex(db)
        if (oldVersion == 1) {
            db.execSQL("ALTER TABLE unhealthy_eating_events ADD COLUMN healthy INTEGER NOT NULL DEFAULT 0 CHECK (healthy IN (0,1))")
        }
        if (oldVersion < 3) createIndex(db)
        if (oldVersion < 4) db.execSQL("ALTER TABLE unhealthy_eating_events ADD COLUMN reasons TEXT NOT NULL DEFAULT ''")
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE unhealthy_eating_events ADD COLUMN event_type TEXT NOT NULL DEFAULT 'UNHEALTHY' CHECK (event_type IN ('UNHEALTHY','HEALTHY','AVOIDED'))")
            db.execSQL("UPDATE unhealthy_eating_events SET event_type = 'HEALTHY' WHERE healthy = 1")
        }
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE unhealthy_eating_events RENAME TO events_v5")
            createEvents(db)
            db.execSQL("INSERT INTO unhealthy_eating_events (id, created_at, event_type, reasons) SELECT id, created_at, event_type, reasons FROM events_v5")
            db.execSQL("DROP TABLE events_v5")
            createIndex(db)
        } else if (oldVersion < 7) {
            db.execSQL("ALTER TABLE unhealthy_eating_events RENAME TO events_v6")
            createEvents(db)
            db.execSQL("INSERT INTO unhealthy_eating_events (id, created_at, event_type, reasons, activity_kind, minutes) SELECT id, created_at, event_type, reasons, activity_kind, minutes FROM events_v6")
            db.execSQL("DROP TABLE events_v6")
            createIndex(db)
        }
        if (oldVersion < 10) addFoodColumns(db)
    }
}
