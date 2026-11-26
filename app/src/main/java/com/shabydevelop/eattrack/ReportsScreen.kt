package com.shabydevelop.eattrack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.*

data class DayCount(val label: String, val count: Long, val healthy: Long = 0, val avoided: Long = 0)
data class EatingReport(val today: Long = 0, val week: Long = 0, val month: Long = 0,
    val total: Long = 0, val days: List<DayCount> = emptyList(),
    val todayLabel: String = "", val weekLabel: String = "", val monthLabel: String = "", val totalLabel: String = "", val healthyToday: Long = 0, val healthyWeek: Long = 0, val healthyMonth: Long = 0, val healthyTotal: Long = 0, val avoidedToday: Long = 0, val avoidedWeek: Long = 0, val avoidedMonth: Long = 0, val avoidedTotal: Long = 0)

@Composable
internal fun ReportsScreen(report: EatingReport, error: Boolean) {
    val maximum = (report.days.maxOfOrNull { maxOf(it.count, it.healthy, it.avoided) } ?: 0).coerceAtLeast(1)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (error) item { Text("לא ניתן לטעון נתונים", color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
        item { Text("סיכום לפי תקופה", style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() }) }
        item { PeriodTable(report.periodRows(true), true) }
        item { Text("שבעת הימים האחרונים", style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() }) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                reportLabels.forEachIndexed { i, label -> Text(label, color = reportColors[i], style = MaterialTheme.typography.labelLarge) }
            }
        }
        items(report.days) { day ->
            val counts = listOf(day.healthy, day.count, day.avoided)
            Row(Modifier.fillMaxWidth().clearAndSetSemantics {
                contentDescription = day.label + ", " + reportLabels.zip(counts).joinToString(", ") { (label, count) -> "$label: $count" }
            }, verticalAlignment = Alignment.CenterVertically) {
                Text(day.label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Column(Modifier.weight(5f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    counts.forEachIndexed { i, count ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f).height(10.dp)) {
                                Box(Modifier.fillMaxWidth(count.toFloat() / maximum.toFloat()).fillMaxHeight().background(reportColors[i]))
                            }
                            Text(count.toString(), Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            HorizontalDivider()
        }
    }
}
