package com.shabydevelop.eattrack

enum class FoodKind(val label: String) { MEAL("ארוחה"), SNACK("נשנוש") }

val EventType.isFood get() = this == EventType.HEALTHY || this == EventType.UNHEALTHY

data class EatingEvent(val id: Long, val timestamp: Long, val type: EventType, val reasons: List<EatingReason> = emptyList(), val activity: ActivityKind? = null, val minutes: Int = 0, val milliliters: Int = 0, val foodKind: FoodKind? = null, val healthyPercent: Int? = if (type.isFood) (if (type == EventType.HEALTHY) 100 else 0) else null)
data class HistoryPage(val events: List<EatingEvent>, val hasMore: Boolean)

val EatingEvent.classifiedType: EventType get() = if (type.isFood) { if ((healthyPercent ?: if (type == EventType.HEALTHY) 100 else 0) >= 50) EventType.HEALTHY else EventType.UNHEALTHY } else type
