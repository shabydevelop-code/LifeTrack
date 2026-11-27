package com.shabydevelop.eattrack

data class DayCount(val label: String, val count: Long, val healthy: Long = 0, val avoided: Long = 0)
data class EatingReport(val today: Long = 0, val week: Long = 0, val month: Long = 0,
    val total: Long = 0, val days: List<DayCount> = emptyList(),
    val todayLabel: String = "", val weekLabel: String = "", val monthLabel: String = "", val totalLabel: String = "", val healthyToday: Long = 0, val healthyWeek: Long = 0, val healthyMonth: Long = 0, val healthyTotal: Long = 0, val avoidedToday: Long = 0, val avoidedWeek: Long = 0, val avoidedMonth: Long = 0, val avoidedTotal: Long = 0, val activity: ActivityReport = ActivityReport(), val water: WaterReport = WaterReport())
