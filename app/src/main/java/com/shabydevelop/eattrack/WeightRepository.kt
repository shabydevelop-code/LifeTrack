package com.shabydevelop.eattrack

import android.content.Context
import android.content.ContentValues

internal data class WeightEntry(val id: Long, val timestamp: Long, val grams: Int)
internal data class WeightPage(val entries: List<WeightEntry>, val hasMore: Boolean)
internal data class WeightSummary(val latest: Int, val change: Int)
internal class WeightRepository(context: Context) : AutoCloseable {
    private val db = EatingDatabase(context.applicationContext)
    fun page(days: Int, after: WeightEntry? = null): WeightPage {
        val clauses = mutableListOf<String>()
        val args = mutableListOf<String>()
        if (days > 0) {
            val start = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
                add(java.util.Calendar.DAY_OF_YEAR, 1 - days)
            }.timeInMillis
            clauses.add("created_at >= ?")
            args.add(start.toString())
        }
        if (after != null) {
            clauses.add("(created_at < ? OR (created_at = ? AND id < ?))")
            args.addAll(listOf(after.timestamp.toString(), after.timestamp.toString(), after.id.toString()))
        }
        val rows = query(clauses.joinToString(" AND ").ifEmpty { null }, args.toTypedArray(), "created_at DESC, id DESC", "31")
        return WeightPage(rows.take(30), rows.size > 30)
    }
    fun summary(): WeightSummary? {
        val latest = query(null, null, "created_at DESC, id DESC", "1").firstOrNull() ?: return null
        val first = query(null, null, "created_at ASC, id ASC", "1").first()
        return WeightSummary(latest.grams, latest.grams - first.grams)
    }
    private fun query(selection: String?, args: Array<String>?, order: String, limit: String): List<WeightEntry> =
        db.readableDatabase.query("weight_entries", arrayOf("id","created_at","grams"), selection,args,null,null,order,limit).use { c ->
            buildList { while (c.moveToNext()) add(WeightEntry(c.getLong(0),c.getLong(1),c.getInt(2))) }
        }
    fun add(grams: Int) {
        require(grams > 0)
        db.writableDatabase.insertOrThrow("weight_entries",null,ContentValues().apply {
            put("created_at",System.currentTimeMillis()); put("grams",grams)
        })
    }
    fun update(entry: WeightEntry) {
        require(entry.grams > 0 && entry.timestamp <= System.currentTimeMillis())
        val values = ContentValues().apply { put("grams", entry.grams); put("created_at", entry.timestamp) }
        if (db.writableDatabase.update("weight_entries", values, "id = ?", arrayOf(entry.id.toString())) != 1)
            throw android.database.SQLException("Weight entry not found")
    }
    fun delete(id: Long) { db.writableDatabase.delete("weight_entries","id = ?",arrayOf(id.toString())) }
    override fun close() = db.close()
}
