package com.shabydevelop.eattrack

data class EatingEvent(val id: Long, val timestamp: Long, val type: EventType, val reasons: List<EatingReason> = emptyList(), val activity: ActivityKind? = null, val minutes: Int = 0, val milliliters: Int = 0)
data class HistoryPage(val events: List<EatingEvent>, val hasMore: Boolean)
