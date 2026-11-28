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
fun ReportsScreen(report: EatingReport, error: Boolean) {
    val scrollState = androidx.compose.foundation.lazy.rememberLazyListState()
    LazyColumn(Modifier.fillMaxSize().lazyScrollIndicator(scrollState), state = scrollState, contentPadding = PaddingValues(UiSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(UiSpacing.normal)) {
        if (error) item { Text("לא ניתן לטעון נתונים", color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
        item { SectionHeader("סיכום דפוסי אכילה") }
        item { PeriodTable(report.periodRows(true), false) }
        item { EatingChart(report.days) }
        item { WaterSummary(report.water) }
        item { WaterChart(report.water.days) }
        item { ActivitySummary(report.activity) }
        item { ActivityChart(report.activity.days) }
    }
}

@Composable
private fun EatingChart(days: List<DayCount>) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
        SectionHeader("דפוסי אכילה – 7 ימים")
        PeriodTable(days.map { PeriodRow(it.label, "", listOf(it.healthy, it.count, it.avoided), it.food) }, false, "תאריך")
    }
}
