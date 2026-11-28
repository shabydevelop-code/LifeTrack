package com.shabydevelop.eattrack

internal class SafeEatingDataSource(private val delegate: EatingDataSource) : EatingDataSource {
    override fun addEvent(type: EventType, activity: ActivityKind?, minutes: Int, milliliters: Int) =
        databaseOperation { delegate.addEvent(type, activity, minutes, milliliters) }
    override fun addFood(kind: FoodKind, percent: Int) = databaseOperation { delegate.addFood(kind, percent) }
    override fun saveReasons(id: Long, reasons: Set<EatingReason>, type: EventType) =
        databaseOperation { delegate.saveReasons(id, reasons, type) }
    override fun updateEvent(event: EatingEvent) = databaseOperation { delegate.updateEvent(event) }
    override fun deleteEvent(id: Long) = databaseOperation { delegate.deleteEvent(id) }
    override fun undoLast() = databaseOperation { delegate.undoLast() }
    override fun reset() = databaseOperation { delegate.reset() }
    override fun report() = databaseOperation { delegate.report() }
    override fun historyPage(days: Int?, type: EventType?, before: EatingEvent?) =
        databaseOperation { delegate.historyPage(days, type, before) }
    override fun count(type: EventType) = databaseOperation { delegate.count(type) }
}
