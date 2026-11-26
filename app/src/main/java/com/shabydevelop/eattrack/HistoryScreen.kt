package com.shabydevelop.eattrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

data class EatingEvent(val id: Long, val timestamp: Long, val type: EventType, val reasons: List<EatingReason> = emptyList())
data class HistoryPage(val events: List<EatingEvent>, val hasMore: Boolean)

@Composable
internal fun HistoryScreen(model: CounterViewModel) {
    var period by rememberSaveable { mutableIntStateOf(7) }
    var type by rememberSaveable { mutableIntStateOf(0) }
    var pageCount by remember { mutableIntStateOf(1) }
    var rows by remember { mutableStateOf(emptyList<EatingEvent>()) }
    var more by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }
    val days = period.takeIf { it > 0 }
    val eventType = when (type) { 1 -> EventType.HEALTHY; 2 -> EventType.UNHEALTHY; 3 -> EventType.AVOIDED; else -> null }
    LaunchedEffect(period, type, model.successSequence, pageCount, retry) {
        loading = true
        error = false
        try {
            val page = model.historyPage(days, eventType, if (pageCount == 1) null else rows.lastOrNull())
            rows = if (pageCount == 1) page.events else rows + page.events
            more = page.hasMore
        } catch (e: android.database.SQLException) { error = true }
        finally { loading = false }
    }
    val dateFormat = remember { java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()) }
    val timeFormat = remember { java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()) }
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HistoryFilter("תקופה", listOf(7 to "7 ימים", 30 to "30 ימים", 0 to "הכל"), period) {
            period = it; pageCount = 1; rows = emptyList(); more = false; loading = true
        }
        HistoryFilter("סוג דיווח", listOf(0 to "הכל", 1 to "אכילה בריאה", 2 to "אכילה לא בריאה", 3 to "ויתור על פיתוי"), type) {
            type = it; pageCount = 1; rows = emptyList(); more = false; loading = true
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(rows, key = { _, event -> event.id }) { index, event ->
                val date = dateFormat.format(java.util.Date(event.timestamp))
                if (index == 0 || date != dateFormat.format(java.util.Date(rows[index - 1].timestamp))) {
                    Text(date, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
                }
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp).semantics(mergeDescendants = true) {}) {
                    Text(timeFormat.format(java.util.Date(event.timestamp)), Modifier.weight(1f))
                    Text(event.type.label, Modifier.weight(2f),
                        color = when (event.type) { EventType.HEALTHY -> Color(0xFF166534); EventType.UNHEALTHY -> Color(0xFFB91C1C); EventType.AVOIDED -> Color(0xFF1D4ED8) })
                }
                if (event.reasons.isNotEmpty()) Text("סיבות: " + event.reasons.joinToString(" · ") { it.label }, style = MaterialTheme.typography.bodyMedium)
                HorizontalDivider()
            }
            if (loading) item { Text("טוען דיווחים", Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
            if (error) item { Text("לא ניתן לטעון דיווחים", Modifier.semantics { liveRegion = LiveRegionMode.Polite }); OutlinedButton(onClick = { retry++ }) { Text("נסה שוב") } }
            if (!loading && !error && rows.isEmpty()) item { Text("אין דיווחים בתקופה שנבחרה") }
            if (more && !loading && !error) item { OutlinedButton(onClick = { loading = true; pageCount++ }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("טעינת דיווחים נוספים") } }
        }
    }
}

@Composable
private fun HistoryFilter(label: String, options: List<Pair<Int, String>>, selected: Int, select: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text(label + ": " + options.first { it.first == selected }.second)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, title) ->
                DropdownMenuItem(text = { Text(title) }, onClick = { expanded = false; if (value != selected) select(value) },
                    modifier = Modifier.semantics { this.selected = value == selected })
            }
        }
    }
}
