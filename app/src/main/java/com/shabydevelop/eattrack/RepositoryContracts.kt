package com.shabydevelop.eattrack

interface EatingDataSource {
    fun addEvent(type: EventType, activity: ActivityKind? = null, minutes: Int = 0, milliliters: Int = 0): Long
    fun saveReasons(id: Long, reasons: Set<EatingReason>, type: EventType)
    fun updateEvent(event: EatingEvent)
    fun deleteEvent(id: Long): Boolean
    fun undoLast(): Boolean
    fun reset()
    fun report(): EatingReport
    fun historyPage(days: Int?, type: EventType?, before: EatingEvent?): HistoryPage
    fun count(type: EventType): Long
}

internal interface WeightDataSource {
    fun page(days: Int, after: WeightEntry? = null): WeightPage
    fun summary(): WeightSummary?
    fun add(grams: Int)
    fun update(entry: WeightEntry)
    fun delete(id: Long)
}
