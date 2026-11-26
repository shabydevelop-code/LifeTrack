package com.shabydevelop.eattrack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

internal val reportLabels = listOf("בריאה", "לא בריאה", "ויתור")
internal val reportColors = listOf(UiColors.healthy, UiColors.unhealthy, UiColors.avoided)
internal data class PeriodRow(val label: String, val range: String, val counts: List<Long>)

internal fun EatingReport.periodRows(includeToday: Boolean): List<PeriodRow> = listOf(
    PeriodRow("היום", todayLabel, listOf(healthyToday, today, avoidedToday)),
    PeriodRow("השבוע", weekLabel, listOf(healthyWeek, week, avoidedWeek)),
    PeriodRow("החודש", monthLabel, listOf(healthyMonth, month, avoidedMonth)),
    PeriodRow("מצטבר", totalLabel, listOf(healthyTotal, total, avoidedTotal))
).let { if (includeToday) it else it.drop(1) }

@Composable
internal fun TableLine(weights: List<Float>, modifier: Modifier = Modifier, cell: @Composable (Int) -> Unit) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        weights.forEachIndexed { index, weight ->
            Box(Modifier.weight(weight).padding(horizontal = UiSpacing.cell), contentAlignment = if (index == 0) Alignment.CenterStart else Alignment.Center) { cell(index) }
        }
    }
}

@Composable
internal fun TableHeading(labels: List<String>, weights: List<Float>, colors: List<Color>? = null) {
    TableLine(weights, Modifier.background(MaterialTheme.colorScheme.surfaceVariant).padding(vertical = UiSpacing.normal).semantics { heading() }) { i ->
        Text(labels[i], style = MaterialTheme.typography.labelLarge, color = colors?.getOrNull(i) ?: MaterialTheme.colorScheme.onSurfaceVariant, textAlign = if (i == 0) TextAlign.Start else TextAlign.Center)
    }
}

@Composable
internal fun PeriodTable(rows: List<PeriodRow>, showRanges: Boolean) {
    val weights = if (showRanges) listOf(1.5f, 1f, 1.15f, 1f) else listOf(1f, 1f, 1.15f, 1f)
    OutlinedCard(Modifier.fillMaxWidth()) {
        TableHeading(listOf("תקופה") + reportLabels, weights, listOf(MaterialTheme.colorScheme.onSurfaceVariant) + reportColors)
        rows.forEach { row ->
            HorizontalDivider()
            TableLine(weights, Modifier.padding(vertical = UiSpacing.normal).clearAndSetSemantics {
                contentDescription = row.label + (if (showRanges) ", " + row.range else "") + ", " + reportLabels.zip(row.counts).joinToString(", ") { (label, count) -> "$label: $count" }
            }) { i ->
                if (i == 0) Column {
                    Text(row.label, style = MaterialTheme.typography.bodyMedium)
                    if (showRanges) Text(row.range, style = MaterialTheme.typography.bodySmall)
                } else Text(row.counts[i-1].toString(), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
