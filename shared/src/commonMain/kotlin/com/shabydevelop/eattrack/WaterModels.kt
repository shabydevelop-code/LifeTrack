package com.shabydevelop.eattrack

data class WaterStats(val count: Long = 0, val milliliters: Long = 0)
data class WaterDay(val label: String, val stats: WaterStats)
data class WaterReport(val today: WaterStats = WaterStats(), val week: WaterStats = WaterStats(), val month: WaterStats = WaterStats(), val total: WaterStats = WaterStats(), val days: List<WaterDay> = emptyList())
