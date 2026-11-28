package com.shabydevelop.eattrack

data class PeriodRow(val label: String, val range: String, val counts: List<Long>, val food: FoodStats = FoodStats())

fun EatingReport.periodRows(includeToday: Boolean): List<PeriodRow> = listOf(
    PeriodRow("היום", todayLabel, listOf(healthyToday, today, avoidedToday), foodToday),
    PeriodRow("השבוע", weekLabel, listOf(healthyWeek, week, avoidedWeek), foodWeek),
    PeriodRow("החודש", monthLabel, listOf(healthyMonth, month, avoidedMonth), foodMonth),
    PeriodRow("כל הזמנים", totalLabel, listOf(healthyTotal, total, avoidedTotal), foodTotal)
).let { if (includeToday) it else it.drop(1) }
