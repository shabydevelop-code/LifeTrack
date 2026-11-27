package com.shabydevelop.eattrack

import android.content.Context
import android.content.ContentValues

internal data class WeightEntry(val id: Long, val timestamp: Long, val grams: Int)
internal class WeightRepository(context: Context) : AutoCloseable {
    private val db = EatingDatabase(context.applicationContext)
    fun list(): List<WeightEntry> = db.readableDatabase.query("weight_entries", arrayOf("id","created_at","grams"), null,null,null,null,"created_at DESC, id DESC").use { c ->
        buildList { while (c.moveToNext()) add(WeightEntry(c.getLong(0),c.getLong(1),c.getInt(2))) }
    }
    fun add(grams: Int) {
        require(grams > 0)
        db.writableDatabase.insertOrThrow("weight_entries",null,ContentValues().apply {
            put("created_at",System.currentTimeMillis()); put("grams",grams)
        })
    }
    fun delete(id: Long) { db.writableDatabase.delete("weight_entries","id = ?",arrayOf(id.toString())) }
    override fun close() = db.close()
}
