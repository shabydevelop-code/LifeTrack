package com.shabydevelop.eattrack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.KeyboardType

data class WaterStats(val count: Long = 0, val milliliters: Long = 0)
data class WaterDay(val label: String, val stats: WaterStats)
data class WaterReport(val today: WaterStats = WaterStats(), val week: WaterStats = WaterStats(), val month: WaterStats = WaterStats(), val total: WaterStats = WaterStats(), val days: List<WaterDay> = emptyList())

@Composable
internal fun WaterAmountField(value: String, change: (String) -> Unit, enabled: Boolean) {
    OutlinedTextField(value = value, onValueChange = { change(it.filter(Char::isDigit)) }, enabled = enabled, singleLine = true,
        label = { Text("כמות במ״ל") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = (value.toIntOrNull() ?: 0) <= 0, modifier = Modifier.fillMaxWidth())
}

@Composable
internal fun WaterDialog(busy: Boolean, dismiss: () -> Unit, save: (Int) -> Unit) {
    var amount by rememberSaveable { mutableStateOf("250") }
    AlertDialog(onDismissRequest = { if (!busy) dismiss() },
        title = { Text("שתיית מים", color = UiColors.water, style = UiTypography.sectionTitle) },
        text = {
            Column(Modifier.visibleVerticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
                listOf(250, 500, 1000).forEach { ml ->
                    AppOutlinedButton(onClick = { amount = ml.toString() }, enabled = !busy, color = UiColors.water, modifier = Modifier.fillMaxWidth()) {
                        Text("$ml מ״ל", style = UiTypography.secondaryAction)
                    }
                }
                WaterAmountField(amount, { amount = it }, !busy)
            }
        },
        confirmButton = { AppTextButton(enabled = !busy && (amount.toIntOrNull() ?: 0) > 0, onClick = { save(amount.toInt()) }) { Text("שמירה") } },
        dismissButton = { AppTextButton(enabled = !busy, onClick = dismiss) { Text("ביטול") } })
}

@Composable
internal fun WaterSummary(report: WaterReport) {
    val entries = listOf("היום" to report.today, "השבוע" to report.week, "החודש" to report.month, "מצטבר" to report.total)
    val weights = List(4) { 1f }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
        SectionHeader("סיכום שתיית מים (מ״ל)", UiColors.water)
        OutlinedCard(Modifier.fillMaxWidth()) {
            TableHeading(entries.map { it.first }, weights)
            HorizontalDivider()
            TableLine(weights, Modifier.padding(vertical = UiSpacing.normal)) { i ->
                MetricValue(entries[i].second.milliliters.toString(), UiColors.water,
                    modifier = Modifier.clearAndSetSemantics { contentDescription = "${entries[i].first}: ${entries[i].second.milliliters} מיליליטר" })
            }
        }
    }
}

@Composable
internal fun WaterChart(days: List<WaterDay>) {
    val max = (days.maxOfOrNull { it.stats.milliliters } ?: 0L).coerceAtLeast(1L)
    val weights = listOf(1f, 3f, 1f)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
        SectionHeader("שתיית מים – 7 ימים", UiColors.water)
        OutlinedCard(Modifier.fillMaxWidth()) {
            TableHeading(listOf("תאריך", "כמות השתייה", "מ״ל"), weights, listOf(MaterialTheme.colorScheme.onSurfaceVariant, UiColors.water, UiColors.water))
            days.forEach { day ->
                HorizontalDivider()
                TableLine(weights, Modifier.padding(vertical = UiSpacing.normal).clearAndSetSemantics {
                    contentDescription = "${day.label}: ${day.stats.milliliters} מיליליטר"
                }) { i ->
                    when (i) {
                        0 -> Text(day.label, style = UiTypography.tableCell)
                        1 -> Box(Modifier.fillMaxWidth().height(UiSpacing.medium)) {
                            Box(Modifier.fillMaxWidth(day.stats.milliliters.toFloat() / max.toFloat()).fillMaxHeight().background(UiColors.water))
                        }
                        else -> MetricValue(day.stats.milliliters.toString(), UiColors.water)
                    }
                }
            }
        }
    }
}
