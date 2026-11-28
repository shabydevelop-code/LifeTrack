package com.shabydevelop.eattrack

enum class ActivityKind(val label: String) {
    WALK("הליכה"), RUN("ריצה"), BIKE("רכיבה על אופניים"), SWIM("שחייה"), OTHER("אחר")
}
data class ActivityStats(val count: Long = 0, val minutes: Long = 0)
data class ActivityDuration(val kind: ActivityKind, val minutes: Long)
data class ActivityDay(val label: String, val stats: ActivityStats, val durations: List<ActivityDuration> = emptyList())
data class ActivityReport(val today: ActivityStats = ActivityStats(), val week: ActivityStats = ActivityStats(), val month: ActivityStats = ActivityStats(), val total: ActivityStats = ActivityStats(), val days: List<ActivityDay> = emptyList())
