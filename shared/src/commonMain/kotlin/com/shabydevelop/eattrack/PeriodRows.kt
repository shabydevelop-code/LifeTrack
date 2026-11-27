package com.shabydevelop.eattrack

data class PeriodRow(val label: String, val range: String, val counts: List<Long>)

fun EatingReport.periodRows(includeToday: Boolean): List<PeriodRow> = listOf(
    PeriodRow("היום", todayLabel, listOf(healthyToday, today, avoidedToday)),
    PeriodRow("השבוע", weekLabel, listOf(healthyWeek, week, avoidedWeek)),
    PeriodRow("החודש", monthLabel, listOf(healthyMonth, month, avoidedMonth)),
    PeriodRow("מצטבר", totalLabel, listOf(healthyTotal, total, avoidedTotal))
).let { if (includeToday) it else it.drop(1) }
