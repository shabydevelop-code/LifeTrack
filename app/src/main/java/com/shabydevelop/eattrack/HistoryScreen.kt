package com.shabydevelop.eattrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalDensity
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
    Column(Modifier.fillMaxSize().padding(UiSpacing.screen), verticalArrangement = Arrangement.spacedBy(UiSpacing.normal)) {
        val filters: @Composable () -> Unit = {
            HistoryFilter("תקופה", listOf(7 to "7 ימים", 30 to "30 ימים", 0 to "הכל"), period) {
                period = it; pageCount = 1; rows = emptyList(); more = false; loading = true
            }
        }
        val types: @Composable () -> Unit = {
            HistoryFilter("סוג", listOf(0 to "הכל", 1 to "בריאה", 2 to "לא בריאה", 3 to "ויתור"), type) {
                type = it; pageCount = 1; rows = emptyList(); more = false; loading = true
            }
        }
        AdaptiveActionPair(filters, types)
        val weights = listOf(0.8f, 1.5f, 2f)
        TableHeading(listOf("שעה", "סוג דיווח", "סיבות"), weights)
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(UiSpacing.normal)) {
            itemsIndexed(rows, key = { _, event -> event.id }) { index, event ->
                val date = dateFormat.format(java.util.Date(event.timestamp))
                if (index == 0 || date != dateFormat.format(java.util.Date(rows[index - 1].timestamp))) {
                    Text(date, style = UiTypography.sectionTitle, modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(UiSpacing.normal).semantics { heading() })
                }
                val time = timeFormat.format(java.util.Date(event.timestamp))
                val reasons = event.reasons.joinToString(" · ") { it.label }
                TableLine(weights, Modifier.padding(vertical = UiSpacing.normal).clearAndSetSemantics {
                    contentDescription = "$date, שעה $time, ${event.type.label}, " + if (reasons.isEmpty()) "לא צוינו סיבות" else "סיבות: $reasons"
                }) { i ->
                    when (i) {
                        0 -> Text(time, style = UiTypography.tableCell)
                        1 -> Text(event.type.label, style = UiTypography.tableCell, color = when (event.type) { EventType.HEALTHY -> reportColors[0]; EventType.UNHEALTHY -> reportColors[1]; EventType.AVOIDED -> reportColors[2] })
                        else -> Text(reasons.ifEmpty { "—" }, style = UiTypography.tableCell)
                    }
                }
                HorizontalDivider()
            }
            if (loading) item { Text("טוען דיווחים", Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
            if (error) item { Text("לא ניתן לטעון דיווחים", Modifier.semantics { liveRegion = LiveRegionMode.Polite }); OutlinedButton(onClick = { retry++ }) { Text("נסה שוב") } }
            if (!loading && !error && rows.isEmpty()) item { Text("אין דיווחים בתקופה שנבחרה") }
            if (more && !loading && !error) item { OutlinedButton(onClick = { loading = true; pageCount++ }, modifier = Modifier.fillMaxWidth().heightIn(min = UiSpacing.touch)) { Text("טעינת דיווחים נוספים") } }
        }
    }
}

@Composable
private fun HistoryFilter(label: String, options: List<Pair<Int, String>>, selected: Int, select: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth(), propagateMinConstraints = true) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth().heightIn(min = UiSpacing.touch)) {
            Text(label + ": " + options.first { it.first == selected }.second, style = UiTypography.secondaryAction, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, title) ->
                DropdownMenuItem(text = { Text(title, style = UiTypography.body) }, onClick = { expanded = false; if (value != selected) select(value) },
                    modifier = Modifier.semantics { this.selected = value == selected })
            }
        }
    }
}
