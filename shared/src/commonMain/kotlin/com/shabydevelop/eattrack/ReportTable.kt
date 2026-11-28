package com.shabydevelop.eattrack

import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

val reportLabels = listOf("בריאה", "לא בריאה", "ויתור")
val reportColors = listOf(UiColors.healthy, UiColors.unhealthy, UiColors.avoided)
@Composable
fun TableLine(weights: List<Float>, modifier: Modifier = Modifier, cell: @Composable (Int) -> Unit) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        weights.forEachIndexed { index, weight ->
            Box(Modifier.weight(weight).padding(start = if (index == 0) UiSpacing.normal else UiSpacing.cell, end = UiSpacing.cell), contentAlignment = if (index == 0) Alignment.CenterStart else Alignment.Center) { cell(index) }
        }
    }
}

@Composable
fun TableFrame(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    OutlinedCard(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, content = content)
}

@Composable
fun Modifier.tableHeaderBackground(): Modifier =
    background(MaterialTheme.colorScheme.surfaceVariant)

// Match the Material outlined card shape and border while retaining lazy rows.
@Composable
fun Modifier.tableSegment(top: Boolean = false, bottom: Boolean = false): Modifier {
    val cardShape = MaterialTheme.shapes.medium
    val zero = CornerSize(0.dp)
    val shape = cardShape.copy(
        topStart = if (top) cardShape.topStart else zero,
        topEnd = if (top) cardShape.topEnd else zero,
        bottomStart = if (bottom) cardShape.bottomStart else zero,
        bottomEnd = if (bottom) cardShape.bottomEnd else zero
    )
    val color = MaterialTheme.colorScheme.outlineVariant
    return clip(shape).drawWithContent {
        drawContent()
        val w = 1.dp.toPx()
        val extra = w * 2
        val outline = shape.createOutline(
            Size(size.width - w, size.height - w + (if (top) 0f else extra) + (if (bottom) 0f else extra)),
            layoutDirection, this
        )
        clipRect {
            translate(w / 2, w / 2 - if (top) 0f else extra) {
                drawOutline(outline, color, style = Stroke(w))
            }
        }
    }
}

@Composable
fun TableHeading(labels: List<String>, weights: List<Float>) {
    TableLine(weights, Modifier.tableHeaderBackground().padding(vertical = UiSpacing.normal).semantics { heading() }) { i ->
        Text(labels[i], style = UiTypography.tableHeading, color = Color.Black, textAlign = if (i == 0) TextAlign.Start else TextAlign.Center)
    }
}

@Composable
fun MetricValue(value: String, color: Color, modifier: Modifier = Modifier) {
    Text(value, modifier = modifier, color = color, style = UiTypography.tableCell, textAlign = TextAlign.Center)
}

private fun FoodGroup.percentText(): String = average?.let { "$it%" } ?: "—"
private fun FoodGroup.summaryText(): String = "$count · ${percentText()}"

@Composable
fun PeriodTable(rows: List<PeriodRow>, showRanges: Boolean, firstLabel: String = "תקופה") {
    val weights = listOf(1.4f, 1f, 1f, 1f, 0.8f)
    val labels = listOf(firstLabel, "ארוחות", "נשנושים", "בריאות", "ויתור")
    val neutral = MaterialTheme.colorScheme.onSurface
    TableFrame {
        TableHeading(labels, weights)
        rows.forEach { row ->
            val food = row.food
            val values = listOf(food.meals.summaryText(), food.snacks.summaryText(), food.overall.percentText(), row.counts[2].toString())
            HorizontalDivider()
            TableLine(weights, Modifier.padding(vertical = UiSpacing.normal).clearAndSetSemantics {
                contentDescription = row.label + (if (showRanges) ", " + row.range else "") +
                    ", ארוחות: ${food.meals.count}, בריאות ארוחות: ${food.meals.percentText()}, נשנושים: ${food.snacks.count}, בריאות נשנושים: ${food.snacks.percentText()}, בריאות כללית: ${food.overall.percentText()}, טרם סווגו: ${food.unclassified.count}, ויתורים: ${row.counts[2]}"
            }) { i ->
                if (i == 0) Column {
                    Text(row.label, style = UiTypography.tableCell)
                    if (showRanges) Text(row.range, style = UiTypography.supporting)
                    if (food.unclassified.count > 0) Text("טרם סווגו: ${food.unclassified.count}", style = UiTypography.supporting)
                } else MetricValue(values[i-1], when(i) { 3 -> UiColors.healthy; 4 -> UiColors.avoided; else -> neutral })
            }
        }
    }
    Text("ארוחות ונשנושים: כמות · ממוצע בריאות. הממוצע הכללי כולל גם דיווחים שטרם סווגו.", style = UiTypography.supporting)
}

private data class DailySummaryRow(val label: String, val value: String, val color: Color)

@Composable
fun DailySummary(report: EatingReport) {
    val rows = listOf(
        DailySummaryRow("ארוחות", report.foodToday.meals.summaryText(), MaterialTheme.colorScheme.onSurface),
        DailySummaryRow("נשנושים", report.foodToday.snacks.summaryText(), MaterialTheme.colorScheme.onSurface),
        DailySummaryRow("בריאות ממוצעת", report.foodToday.overall.percentText(), UiColors.healthy),
        DailySummaryRow("טרם סווגו", report.foodToday.unclassified.count.toString(), MaterialTheme.colorScheme.onSurface),
        DailySummaryRow("ויתור על פיתוי", report.avoidedToday.toString(), UiColors.avoided),
        DailySummaryRow("שתיית מים", "${report.water.today.milliliters} מ״ל", UiColors.water),
        DailySummaryRow("פעילות אירובית", "${report.activity.today.minutes} דק׳", UiColors.aerobic)
    )
    val weights = listOf(2f, 1f)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(UiSpacing.normal)) {
        SectionHeader("סיכום יומי")
        TableFrame {
            TableHeading(listOf("סוג דיווח", "ערך"), weights)
            rows.forEach { row ->
                HorizontalDivider()
                TableLine(weights, Modifier.padding(vertical = UiSpacing.normal)
                    .clearAndSetSemantics { contentDescription = "${row.label}: ${row.value}" }) { i ->
                    if (i == 0) Text(row.label, color = row.color, style = UiTypography.tableCell)
                    else MetricValue(row.value, row.color)
                }
            }
        }
    }
}
