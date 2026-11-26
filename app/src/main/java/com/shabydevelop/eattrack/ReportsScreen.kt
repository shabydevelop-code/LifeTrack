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
import androidx.compose.ui.semantics.semantics

data class DayCount(val label: String, val count: Long, val healthy: Long = 0)
data class EatingEvent(val timestamp: Long, val healthy: Boolean)
data class EatingReport(val today: Long = 0, val week: Long = 0, val month: Long = 0,
    val total: Long = 0, val days: List<DayCount> = emptyList(), val history: List<EatingEvent> = emptyList(),
    val todayLabel: String = "", val weekLabel: String = "", val monthLabel: String = "", val totalLabel: String = "", val healthyToday: Long = 0, val healthyWeek: Long = 0, val healthyMonth: Long = 0, val healthyTotal: Long = 0)

@Composable
internal fun ReportsScreen(report: EatingReport, error: Boolean, reset: () -> Unit, busy: Boolean) {
    var confirmReset by remember { mutableStateOf(false) }
    if (confirmReset) AlertDialog(
        onDismissRequest = { confirmReset = false },
        title = { Text("מחיקת היסטוריית דיווחים") },
        text = { Text("כל הדיווחים יימחקו לצמיתות. להמשיך?") },
        confirmButton = { TextButton(onClick = { confirmReset = false; reset() }) { Text("מחיקה") } },
        dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("ביטול") } }
    )
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (error) item { Text("לא ניתן לטעון נתונים", color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
        item { ReportSummary("היום", report.todayLabel, report.today, report.healthyToday) }
        item { ReportSummary("השבוע", report.weekLabel, report.week, report.healthyWeek) }
        item { ReportSummary("החודש", report.monthLabel, report.month, report.healthyMonth) }
        item { ReportSummary("סה״כ דיווחים", report.totalLabel, report.total, report.healthyTotal) }
        item { OutlinedButton(onClick = { confirmReset = true }, enabled = !busy && report.total + report.healthyTotal > 0) { Text("מחיקת היסטוריית דיווחים") } }
        item { Text("שבעת הימים האחרונים", style = MaterialTheme.typography.titleLarge) }
        items(report.days) { day ->
            val max = (report.days.maxOfOrNull { maxOf(it.count, it.healthy) } ?: 0).coerceAtLeast(1)
            Column {
                Text(day.label, style = MaterialTheme.typography.titleMedium)
                listOf(Triple("בריאה", day.healthy, Color(0xFF166534)), Triple("לא בריאה", day.count, Color(0xFFB91C1C))).forEach { (label, count, color) ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(label, Modifier.weight(1f))
                        Box(Modifier.weight(2f).height(14.dp)) {
                            Box(Modifier.fillMaxWidth(count.toFloat() / max.toFloat()).fillMaxHeight().background(color))
                        }
                        Text(count.toString(), Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
        item { Text("היסטוריית דיווחים", style = MaterialTheme.typography.titleLarge) }
        if (report.history.isEmpty()) item { Text("אין דיווחים עדיין") }
        items(report.history) { event ->
            Text(java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(event.timestamp)) + if (event.healthy) " – אכילה בריאה" else " – אכילה לא בריאה")
            HorizontalDivider()
        }
    }
}

@Composable
internal fun ReportSummary(label: String, range: String, count: Long, healthy: Long) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium)
                Text(range, style = MaterialTheme.typography.bodySmall)
            }
            Column { Text("בריאה: $healthy", color = Color(0xFF166534)); Text("לא בריאה: $count", color = Color(0xFFB91C1C)) }
        }
    }
}
