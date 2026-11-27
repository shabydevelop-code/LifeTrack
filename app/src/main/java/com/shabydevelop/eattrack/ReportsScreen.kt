package com.shabydevelop.eattrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.*

@Composable
internal fun ReportsScreen(report: EatingReport, error: Boolean) {
    val scrollState = androidx.compose.foundation.lazy.rememberLazyListState()
    LazyColumn(Modifier.fillMaxSize().lazyScrollIndicator(scrollState), state = scrollState, contentPadding = PaddingValues(UiSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(UiSpacing.normal)) {
        if (error) item { Text("לא ניתן לטעון נתונים", color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
        item { SectionHeader("סיכום דפוסי אכילה") }
        item { PeriodTable(report.periodRows(true), true) }
        item { EatingChart(report.days) }
        item { WaterSummary(report.water) }
        item { WaterChart(report.water.days) }
        item { ActivitySummary(report.activity) }
        item { ActivityChart(report.activity.days) }
    }
}

@Composable
private fun EatingChart(days: List<DayCount>) {
    val weights = listOf(1f, 1f, 1.15f, 1f)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
        SectionHeader("דפוסי אכילה – 7 ימים")
        OutlinedCard(Modifier.fillMaxWidth()) {
            TableHeading(listOf("תאריך") + reportLabels, weights,
                listOf(MaterialTheme.colorScheme.onSurfaceVariant) + reportColors)
            days.forEach { day ->
                val counts = listOf(day.healthy, day.count, day.avoided)
                HorizontalDivider()
                TableLine(weights, Modifier.padding(vertical = UiSpacing.normal).clearAndSetSemantics {
                    contentDescription = day.label + ", " + reportLabels.zip(counts).joinToString(", ") { (label, count) -> "$label: $count" }
                }) { i ->
                    if (i == 0) Text(day.label, style = UiTypography.tableCell)
                    else {
                        val count = counts[i - 1]
                        val color = reportColors[i - 1]
                        MetricValue(count.toString(), color)
                    }
                }
            }
        }
    }
}
