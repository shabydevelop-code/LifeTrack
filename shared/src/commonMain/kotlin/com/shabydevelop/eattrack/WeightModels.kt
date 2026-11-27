package com.shabydevelop.eattrack

data class WeightEntry(val id: Long, val timestamp: Long, val grams: Int)
data class WeightPage(val entries: List<WeightEntry>, val hasMore: Boolean)
data class WeightSummary(val latest: Int, val change: Int)
