package com.shabydevelop.eattrack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp

@Composable
fun ActivityDialog(busy: Boolean, dismiss: () -> Unit, save: (ActivityKind, Int) -> Unit) {
    var kind by rememberSaveable { mutableStateOf(ActivityKind.WALK.name) }
    var minutes by rememberSaveable { mutableIntStateOf(30) }
    AlertDialog(onDismissRequest = dismiss,
        title = { Text("פעילות אירובית", color = UiColors.aerobic, style = UiTypography.sectionTitle) },
        text = {
            Column(Modifier.visibleVerticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(UiSpacing.normal)) {
                Text("סוג הפעילות", color = UiColors.aerobic, style = UiTypography.body)
                Column(Modifier.selectableGroup()) {
                    ActivityKind.entries.forEach { item ->
                        Row(Modifier.fillMaxWidth().heightIn(min = UiSpacing.touch).selectable(kind == item.name, role = Role.RadioButton, onClick = { kind = item.name }), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = kind == item.name, onClick = null)
                            Text(item.label, style = UiTypography.body)
                        }
                    }
                }
                Text("משך הפעילות בדקות", color = UiColors.aerobic, style = UiTypography.body)
                Column(Modifier.selectableGroup()) {
                    (10..60 step 10).toList().chunked(3).forEach { pair ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
                            pair.forEach { value ->
                                Row(Modifier.weight(1f).heightIn(min = UiSpacing.touch).selectable(minutes == value, role = Role.RadioButton, onClick = { minutes = value }), verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = minutes == value, onClick = null)
                                    Text(value.toString(), style = UiTypography.body)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { AppTextButton(enabled = !busy, onClick = { save(ActivityKind.valueOf(kind), minutes) }) { Text("שמירה", style = UiTypography.secondaryAction) } },
        dismissButton = { AppTextButton(onClick = dismiss) { Text("ביטול", style = UiTypography.secondaryAction) } })
}

@Composable
fun ActivitySummary(report: ActivityReport) {
    val entries = listOf("היום" to report.today, "השבוע" to report.week, "החודש" to report.month, "מצטבר" to report.total)
    val weights = List(4) { 1f }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
        SectionHeader("סיכום פעילות אירובית (דק׳)", UiColors.aerobic)
        OutlinedCard(Modifier.fillMaxWidth()) {
            TableHeading(entries.map { it.first }, weights)
            HorizontalDivider()
            TableLine(weights, Modifier.padding(vertical = UiSpacing.normal)) { i ->
                MetricValue(entries[i].second.minutes.toString(), UiColors.aerobic,
                    modifier = Modifier.clearAndSetSemantics { contentDescription = "${entries[i].first}: ${entries[i].second.minutes} דקות" })
            }
        }
    }
}

@Composable
fun ActivityChart(days: List<ActivityDay>) {
    val weights = listOf(1f, 3f, 1f)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
        SectionHeader("פעילות אירובית – 7 ימים", UiColors.aerobic)
        OutlinedCard(Modifier.fillMaxWidth()) {
            TableHeading(listOf("תאריך", "סוג הפעילות", "דקות"), weights)
            days.forEach { day ->
                HorizontalDivider()
                val entries = day.durations.ifEmpty { listOf(ActivityDuration(ActivityKind.OTHER, day.stats.minutes)) }
                entries.forEachIndexed { index, entry ->
                    TableLine(weights, Modifier.padding(vertical = UiSpacing.small).clearAndSetSemantics {
                        contentDescription = "${day.label}: " + (if (entry.minutes == 0L) "ללא פעילות" else entry.kind.label) + ", ${entry.minutes} דקות"
                    }) { i ->
                        when (i) {
                            0 -> if (index == 0) Text(day.label, style = UiTypography.tableCell)
                            1 -> Text(if (entry.minutes == 0L) "—" else entry.kind.label, style = UiTypography.tableCell, color = UiColors.aerobic)
                            else -> MetricValue(entry.minutes.toString(), UiColors.aerobic)
                        }
                    }
                }
            }
        }
    }
}
