package com.shabydevelop.eattrack

data class ReportDay(val start: Long, val end: Long, val label: String)
data class ReportPeriods(val now: Long, val today: Long, val week: Long, val month: Long,
    val todayLabel: String, val weekLabel: String, val monthLabel: String, val days: List<ReportDay>)

fun buildEatingReport(
    periods: ReportPeriods,
    countRange: (Long, Long, EventType) -> Long,
    count: (EventType) -> Long,
    first: Long?,
    formatDate: (Long) -> String,
    activityStats: (Long, Long) -> ActivityStats,
    waterStats: (Long, Long) -> WaterStats,
    foodStats: (Long, Long) -> FoodStats = { _, _ -> FoodStats() },
    activityDurations: (Long, Long) -> List<ActivityDuration> = { _, _ -> emptyList() }
): EatingReport {
    val p = periods
    val end = p.now + 1
    fun since(start: Long, type: EventType = EventType.UNHEALTHY) = countRange(start, end, type)
    val days = p.days.map { d -> DayCount(d.label, countRange(d.start, d.end, EventType.UNHEALTHY),
        countRange(d.start, d.end, EventType.HEALTHY), countRange(d.start, d.end, EventType.AVOIDED), foodStats(d.start, d.end)) }
    return EatingReport(since(p.today), since(p.week), since(p.month), count(EventType.UNHEALTHY), days,
        p.todayLabel, p.weekLabel, p.monthLabel, first?.let { formatDate(it) + " – " + p.todayLabel } ?: "אין דיווחים",
        since(p.today, EventType.HEALTHY), since(p.week, EventType.HEALTHY), since(p.month, EventType.HEALTHY), count(EventType.HEALTHY),
        since(p.today, EventType.AVOIDED), since(p.week, EventType.AVOIDED), since(p.month, EventType.AVOIDED), count(EventType.AVOIDED),
        activity = ActivityReport(activityStats(p.today,end), activityStats(p.week,end), activityStats(p.month,end), activityStats(0,end),
            p.days.map { ActivityDay(it.label, activityStats(it.start,it.end), activityDurations(it.start,it.end)) }),
        water = WaterReport(waterStats(p.today,end), waterStats(p.week,end), waterStats(p.month,end), waterStats(0,end),
            p.days.map { WaterDay(it.label, waterStats(it.start,it.end)) }),
        foodToday = foodStats(p.today,end), foodWeek = foodStats(p.week,end), foodMonth = foodStats(p.month,end), foodTotal = foodStats(0,end))
}
