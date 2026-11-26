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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class ActivityKind(val label: String) {
    WALK("הליכה"), RUN("ריצה"), BIKE("רכיבה על אופניים"), SWIM("שחייה"), OTHER("אחר")
}
data class ActivityStats(val count: Long = 0, val minutes: Long = 0)
data class ActivityDay(val label: String, val stats: ActivityStats)
data class ActivityReport(val today: ActivityStats = ActivityStats(), val week: ActivityStats = ActivityStats(), val month: ActivityStats = ActivityStats(), val total: ActivityStats = ActivityStats(), val days: List<ActivityDay> = emptyList())

@Composable
internal fun ActivityDialog(busy: Boolean, dismiss: () -> Unit, save: (ActivityKind, Int) -> Unit) {
    var kind by rememberSaveable { mutableStateOf(ActivityKind.WALK.name) }
    var minutes by rememberSaveable { mutableIntStateOf(30) }
    AlertDialog(onDismissRequest = dismiss,
        title = { Text("פעילות אירובית", style = UiTypography.sectionTitle) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(UiSpacing.normal)) {
                Text("סוג הפעילות", style = UiTypography.body)
                Column(Modifier.selectableGroup()) {
                    ActivityKind.entries.forEach { item ->
                        Row(Modifier.fillMaxWidth().heightIn(min = UiSpacing.touch).selectable(kind == item.name, role = Role.RadioButton, onClick = { kind = item.name }), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = kind == item.name, onClick = null)
                            Text(item.label, style = UiTypography.body)
                        }
                    }
                }
                Text("משך הפעילות בדקות", style = UiTypography.body)
                Column(Modifier.selectableGroup()) {
                    listOf(15, 30, 45, 60).chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(UiSpacing.normal)) {
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
        confirmButton = { TextButton(enabled = !busy, onClick = { save(ActivityKind.valueOf(kind), minutes) }) { Text("שמירה", style = UiTypography.secondaryAction) } },
        dismissButton = { TextButton(onClick = dismiss) { Text("ביטול", style = UiTypography.secondaryAction) } })
}

@Composable
internal fun ActivitySummary(report: ActivityReport) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
        Text("פעילות אירובית", color = UiColors.aerobic, style = UiTypography.sectionTitle, modifier = Modifier.semantics { heading() })
        OutlinedCard(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(UiSpacing.small)) {
                listOf("היום" to report.today, "השבוע" to report.week, "החודש" to report.month, "מצטבר" to report.total).forEach { (label, stats) ->
                    Column(Modifier.weight(1f).clearAndSetSemantics { contentDescription = "$label: ${stats.count} פעילויות, ${stats.minutes} דקות" }, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(label, style = UiTypography.tableHeading, textAlign = TextAlign.Center)
                        Text(stats.count.toString(), color = UiColors.aerobic, style = UiTypography.counter)
                        Text("${stats.minutes} דק׳", style = UiTypography.supporting, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
internal fun ActivityChart(days: List<ActivityDay>) {
    val max = (days.maxOfOrNull { it.stats.minutes } ?: 0L).coerceAtLeast(1L)
    Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.normal)) {
        Text("פעילות אירובית · דקות בשבעת הימים האחרונים", style = UiTypography.sectionTitle, modifier = Modifier.semantics { heading() })
        days.forEach { day ->
            Row(Modifier.fillMaxWidth().clearAndSetSemantics { contentDescription = "${day.label}: ${day.stats.minutes} דקות, ${day.stats.count} פעילויות" }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(UiSpacing.normal)) {
                Text(day.label, style = UiTypography.tableCell)
                Box(Modifier.weight(1f).height(UiSpacing.medium)) {
                    Box(Modifier.fillMaxWidth(day.stats.minutes.toFloat()/max.toFloat()).fillMaxHeight().background(UiColors.aerobic))
                }
                Text(day.stats.minutes.toString(), style = UiTypography.tableCell)
            }
        }
    }
}
