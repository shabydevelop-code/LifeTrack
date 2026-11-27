package com.shabydevelop.eattrack

import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
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
internal fun TableFrame(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    OutlinedCard(modifier.fillMaxWidth(), content = content)
}

@Composable
internal fun Modifier.tableHeaderBackground(): Modifier =
    background(MaterialTheme.colorScheme.surfaceVariant)

// Continuous outline for a table whose rows are individual lazy-list items.
@Composable
internal fun Modifier.tableSegment(top: Boolean = false, bottom: Boolean = false): Modifier {
    val color = MaterialTheme.colorScheme.outlineVariant
    return drawWithContent {
        drawContent()
        val w = 1.dp.toPx()
        val inset = w / 2
        drawLine(color, Offset(inset, 0f), Offset(inset, size.height), w)
        drawLine(color, Offset(size.width - inset, 0f), Offset(size.width - inset, size.height), w)
        if (top) drawLine(color, Offset(0f, inset), Offset(size.width, inset), w)
        if (bottom) drawLine(color, Offset(0f, size.height - inset), Offset(size.width, size.height - inset), w)
    }
}

@Composable
internal fun TableHeading(labels: List<String>, weights: List<Float>, colors: List<Color>? = null) {
    TableLine(weights, Modifier.tableHeaderBackground().padding(vertical = UiSpacing.normal).semantics { heading() }) { i ->
        Text(labels[i], style = UiTypography.tableHeading, color = colors?.getOrNull(i) ?: MaterialTheme.colorScheme.onSurfaceVariant, textAlign = if (i == 0) TextAlign.Start else TextAlign.Center)
    }
}

@Composable
internal fun MetricValue(value: String, color: Color, modifier: Modifier = Modifier) {
    Text(value, modifier = modifier, color = color, style = UiTypography.tableCell, textAlign = TextAlign.Center)
}

@Composable
internal fun PeriodTable(rows: List<PeriodRow>, showRanges: Boolean) {
    val weights = if (showRanges) listOf(1.5f, 1f, 1.15f, 1f) else listOf(1f, 1f, 1.15f, 1f)
    TableFrame {
        TableHeading(listOf("תקופה") + reportLabels, weights, listOf(MaterialTheme.colorScheme.onSurfaceVariant) + reportColors)
        rows.forEach { row ->
            HorizontalDivider()
            TableLine(weights, Modifier.padding(vertical = UiSpacing.normal).clearAndSetSemantics {
                contentDescription = row.label + (if (showRanges) ", " + row.range else "") + ", " + reportLabels.zip(row.counts).joinToString(", ") { (label, count) -> "$label: $count" }
            }) { i ->
                if (i == 0) Column {
                    Text(row.label, style = UiTypography.tableCell)
                    if (showRanges) Text(row.range, style = UiTypography.supporting)
                } else MetricValue(row.counts[i-1].toString(), reportColors[i-1])
            }
        }
    }
}

private data class DailySummaryRow(val label: String, val value: String, val color: Color)

@Composable
internal fun DailySummary(report: EatingReport) {
    val rows = listOf(
        DailySummaryRow("אכילה בריאה", report.healthyToday.toString(), UiColors.healthy),
        DailySummaryRow("אכילה לא בריאה", report.today.toString(), UiColors.unhealthy),
        DailySummaryRow("ויתור על פיתוי", report.avoidedToday.toString(), UiColors.avoided),
        DailySummaryRow("שתיית מים", "${report.water.today.milliliters} מ״ל", UiColors.water),
        DailySummaryRow("פעילות אירובית", "${report.activity.today.minutes} דק׳", UiColors.aerobic)
    )
    val weights = listOf(2f, 1f)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
        SectionHeader("סיכום יומי")
        TableFrame {
            rows.forEachIndexed { index, row ->
                if (index > 0) HorizontalDivider()
                TableLine(weights, Modifier.padding(vertical = UiSpacing.normal, horizontal = UiSpacing.normal)
                    .clearAndSetSemantics { contentDescription = "${row.label}: ${row.value}" }) { i ->
                    if (i == 0) Text(row.label, color = row.color, style = UiTypography.tableCell)
                    else MetricValue(row.value, row.color)
                }
            }
        }
    }
}
