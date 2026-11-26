package com.shabydevelop.androiddemo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class DayCount(val label: String, val count: Long)
data class EatingReport(val today: Long = 0, val week: Long = 0, val month: Long = 0,
    val total: Long = 0, val days: List<DayCount> = emptyList(), val history: List<Long> = emptyList())

@Composable
internal fun ReportsScreen(report: EatingReport, error: Boolean) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (error) item { Text("לא ניתן לטעון נתונים", color = MaterialTheme.colorScheme.error) }
        items(listOf("היום" to report.today, "השבוע" to report.week, "החודש" to report.month, "סה״כ דיווחים" to report.total)) { (label, count) ->
            OutlinedCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(label, Modifier.weight(1f))
                    Text(count.toString(), style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
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
