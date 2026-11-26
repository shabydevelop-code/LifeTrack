package com.shabydevelop.androiddemo

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

data class DayCount(val label: String, val count: Long)
data class EatingReport(val today: Long = 0, val week: Long = 0, val month: Long = 0,
    val total: Long = 0, val days: List<DayCount> = emptyList(), val history: List<Long> = emptyList(),
    val todayLabel: String = "", val weekLabel: String = "", val monthLabel: String = "", val totalLabel: String = "")

@Composable
internal fun ReportsScreen(report: EatingReport, error: Boolean, reset: () -> Unit) {
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
        if (error) item { Text("לא ניתן לטעון נתונים", color = MaterialTheme.colorScheme.error) }
        item { ReportSummary("היום", report.todayLabel, report.today) }
        item { ReportSummary("השבוע", report.weekLabel, report.week) }
        item { ReportSummary("החודש", report.monthLabel, report.month) }
        item { ReportSummary("סה״כ דיווחים", report.totalLabel, report.total) }
        item { OutlinedButton(onClick = { confirmReset = true }, enabled = report.total > 0) { Text("מחיקת היסטוריית דיווחים") } }
        item { Text("שבעת הימים האחרונים", style = MaterialTheme.typography.titleLarge) }
        items(report.days) { day ->
            val max = (report.days.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(day.label, Modifier.width(58.dp))
                Box(Modifier.weight(1f).height(18.dp)) {
                    Box(Modifier.fillMaxWidth(day.count.toFloat() / max.toFloat()).fillMaxHeight().background(Color(0xFFB91C1C)))
                }
                Text(day.count.toString(), Modifier.padding(start = 8.dp))
            }
        }
        item { Text("היסטוריית דיווחים", style = MaterialTheme.typography.titleLarge) }
        if (report.history.isEmpty()) item { Text("אין דיווחים עדיין") }
        items(report.history) { timestamp ->
            Text(java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(timestamp)))
            HorizontalDivider()
        }
    }
}

@Composable
internal fun ReportSummary(label: String, range: String, count: Long) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium)
                Text(range, style = MaterialTheme.typography.bodySmall)
            }
            Text(count.toString(), style = MaterialTheme.typography.headlineSmall)
        }
    }
}
