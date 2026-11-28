package com.shabydevelop.eattrack

data class DayCount(val label: String, val count: Long, val healthy: Long = 0, val avoided: Long = 0, val food: FoodStats = FoodStats())
data class EatingReport(val today: Long = 0, val week: Long = 0, val month: Long = 0,
    val total: Long = 0, val days: List<DayCount> = emptyList(),
    val todayLabel: String = "", val weekLabel: String = "", val monthLabel: String = "", val totalLabel: String = "", val healthyToday: Long = 0, val healthyWeek: Long = 0, val healthyMonth: Long = 0, val healthyTotal: Long = 0, val avoidedToday: Long = 0, val avoidedWeek: Long = 0, val avoidedMonth: Long = 0, val avoidedTotal: Long = 0, val activity: ActivityReport = ActivityReport(), val water: WaterReport = WaterReport(), val foodToday: FoodStats = FoodStats(), val foodWeek: FoodStats = FoodStats(), val foodMonth: FoodStats = FoodStats(), val foodTotal: FoodStats = FoodStats())

data class FoodGroup(val count: Long = 0, val percentSum: Long = 0) {
    val average: Long? get() = if (count == 0L) null else (percentSum + count / 2) / count
}
data class FoodStats(val meals: FoodGroup = FoodGroup(), val snacks: FoodGroup = FoodGroup(), val unclassified: FoodGroup = FoodGroup()) {
    val overall: FoodGroup get() = FoodGroup(meals.count + snacks.count + unclassified.count, meals.percentSum + snacks.percentSum + unclassified.percentSum)
}
