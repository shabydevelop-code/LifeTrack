package com.shabydevelop.eattrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
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

data class EatingEvent(val id: Long, val timestamp: Long, val type: EventType, val reasons: List<EatingReason> = emptyList(), val activity: ActivityKind? = null, val minutes: Int = 0, val milliliters: Int = 0)
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
    var pendingEdit by remember { mutableStateOf<EatingEvent?>(null) }
    var pendingDelete by remember { mutableStateOf<EatingEvent?>(null) }
    var loadedSequence by remember { mutableLongStateOf(model.successSequence) }
    val days = period.takeIf { it > 0 }
    val eventType = when (type) { 1 -> EventType.HEALTHY; 2 -> EventType.UNHEALTHY; 3 -> EventType.AVOIDED; 4 -> EventType.AEROBIC; 5 -> EventType.WATER; else -> null }
    LaunchedEffect(period, type, model.successSequence, pageCount, retry) {
        if (loadedSequence != model.successSequence) {
            loadedSequence = model.successSequence
            if (pageCount != 1) { pageCount = 1; return@LaunchedEffect }
        }
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
            HistoryFilter("סוג", listOf(0 to "הכל", 1 to "בריאה", 2 to "לא בריאה", 3 to "ויתור", 4 to "אירובית", 5 to "מים"), type) {
                type = it; pageCount = 1; rows = emptyList(); more = false; loading = true
            }
        }
        AdaptiveActionPair(filters, types)
        val measurer = rememberTextMeasurer()
        val timeWidth = with(LocalDensity.current) {
            measurer.measure("88:88", style = UiTypography.tableCell).size.width.toDp() + UiSpacing.normal
        }.coerceAtLeast(UiSpacing.touch)
        TableFrame(Modifier.weight(1f)) {
        Row(Modifier.fillMaxWidth().tableHeaderBackground().padding(end = UiSpacing.normal).padding(vertical = UiSpacing.normal).semantics { heading() }, verticalAlignment = Alignment.CenterVertically) {
            HistoryCells(timeWidth, "שעה", "סוג דיווח", "פרטים", heading = true, modifier = Modifier.weight(1f))
            Text("פעולות", modifier = Modifier.width(UiSpacing.touch * 2), style = UiTypography.tableHeading, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        if (model.error) Text("לא ניתן להשלים את הפעולה או לרענן את הנתונים", color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        val scrollState = androidx.compose.foundation.lazy.rememberLazyListState()
        LazyColumn(Modifier.weight(1f).fillMaxWidth().lazyScrollIndicator(scrollState, reserveSpace = false), state = scrollState, verticalArrangement = Arrangement.spacedBy(UiSpacing.tiny)) {
            itemsIndexed(rows, key = { _, event -> event.id }) { index, event ->
                val date = dateFormat.format(java.util.Date(event.timestamp))
                if (index == 0 || date != dateFormat.format(java.util.Date(rows[index - 1].timestamp))) {
                    Text(date, style = UiTypography.dateHeading, modifier = Modifier.fillMaxWidth().tableHeaderBackground().padding(UiSpacing.small).semantics { heading() })
                }
                val time = timeFormat.format(java.util.Date(event.timestamp))
                val reasons = if (event.type == EventType.AEROBIC) "${event.activity?.label ?: ""} · ${event.minutes} דקות" else if (event.type == EventType.WATER) "${event.milliliters} מ״ל" else event.reasons.joinToString(" · ") { it.label }
                Row(Modifier.fillMaxWidth().padding(end = UiSpacing.normal), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                HistoryCells(timeWidth, time, event.type.label, reasons.ifEmpty { "—" },
                    typeColor = when (event.type) { EventType.HEALTHY -> reportColors[0]; EventType.UNHEALTHY -> reportColors[1]; EventType.AVOIDED -> reportColors[2]; EventType.AEROBIC -> UiColors.aerobic; EventType.WATER -> UiColors.water },
                    modifier = Modifier.weight(1f).clearAndSetSemantics {
                        contentDescription = "$date, שעה $time, ${event.type.label}, " + if (reasons.isEmpty()) "לא צוינו סיבות" else "פרטים: $reasons"
                    })
                IconButton(onClick = { pendingEdit = event }, enabled = !model.busy, modifier = Modifier.size(UiSpacing.touch).semantics { contentDescription = "עריכת דיווח ${event.type.label}, $date, $time" }) {
                    androidx.compose.foundation.Canvas(Modifier.size(UiSpacing.compactIcon)) {
                        val w = UiSpacing.tiny.toPx()
                        fun line(x1: Float, y1: Float, x2: Float, y2: Float) = drawLine(UiColors.primary, androidx.compose.ui.geometry.Offset(size.width*x1,size.height*y1), androidx.compose.ui.geometry.Offset(size.width*x2,size.height*y2), w)
                        line(.15f,.85f,.22f,.55f); line(.22f,.55f,.72f,.05f); line(.72f,.05f,.95f,.28f); line(.95f,.28f,.45f,.78f); line(.45f,.78f,.15f,.85f); line(.62f,.15f,.85f,.38f)
                    }
                }
                IconButton(onClick = { pendingDelete = event }, enabled = !model.busy, modifier = Modifier.size(UiSpacing.touch).semantics { contentDescription = "מחיקת דיווח ${event.type.label}, $date, $time" }) {
                    androidx.compose.foundation.Canvas(Modifier.size(UiSpacing.large)) {
                        val c = UiColors.unhealthy
                        val w = UiSpacing.tiny.toPx()
                        fun line(x1: Float, y1: Float, x2: Float, y2: Float) = drawLine(c, androidx.compose.ui.geometry.Offset(size.width*x1,size.height*y1), androidx.compose.ui.geometry.Offset(size.width*x2,size.height*y2), w)
                        line(.15f,.25f,.85f,.25f); line(.4f,.12f,.6f,.12f)
                        line(.25f,.3f,.3f,.88f); line(.75f,.3f,.7f,.88f); line(.3f,.88f,.7f,.88f)
                        line(.43f,.4f,.43f,.72f); line(.57f,.4f,.57f,.72f)
                    }
                }
                }
                HorizontalDivider()
            }
            if (loading) item { Text("טוען דיווחים", Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }
            if (error) item { Text("לא ניתן לטעון דיווחים", Modifier.semantics { liveRegion = LiveRegionMode.Polite }); AppOutlinedButton(onClick = { retry++ }) { Text("נסה שוב") } }
            if (!loading && !error && rows.isEmpty()) item { Text("אין דיווחים בתקופה שנבחרה") }
            if (more && !loading && !error) item { AppOutlinedButton(onClick = { loading = true; pageCount++ }, modifier = Modifier.fillMaxWidth().padding(end = UiSpacing.normal).heightIn(min = UiSpacing.touch)) { Text("טעינת דיווחים נוספים") } }
        }
        }
    }
    pendingEdit?.let { event -> EditEventDialog(event, model, { pendingEdit = null }) }
    pendingDelete?.let { event ->
        AlertDialog(onDismissRequest = { pendingDelete = null },
            title = { Text("מחיקת דיווח", style = UiTypography.sectionTitle) },
            text = { Text("${event.type.label}\n${dateFormat.format(java.util.Date(event.timestamp))} · ${timeFormat.format(java.util.Date(event.timestamp))}\nהדיווח יימחק לצמיתות. להמשיך?", style = UiTypography.body) },
            confirmButton = { AppTextButton(color = UiColors.unhealthy, enabled = !model.busy, onClick = { pendingDelete = null; model.deleteEvent(event.id) }) { Text("מחיקה", style = UiTypography.secondaryAction) } },
            dismissButton = { AppTextButton(onClick = { pendingDelete = null }) { Text("ביטול", style = UiTypography.secondaryAction) } })
    }
}

@Composable
private fun HistoryFilter(label: String, options: List<Pair<Int, String>>, selected: Int, select: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth(), propagateMinConstraints = true) {
        AppOutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth().heightIn(min = UiSpacing.touch)) {
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

@Composable
private fun HistoryCells(timeWidth: androidx.compose.ui.unit.Dp, time: String, type: String, details: String,
    modifier: Modifier = Modifier, heading: Boolean = false, typeColor: Color = if (heading) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface) {
    val style = if (heading) UiTypography.tableHeading else UiTypography.tableCell
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(time, modifier = Modifier.width(timeWidth), style = if (heading) style else style.copy(textDirection = TextDirection.Ltr),
            color = if (heading) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center, softWrap = false, maxLines = 1)
        Text(type, modifier = Modifier.weight(1f).padding(horizontal = UiSpacing.cell), style = style, color = typeColor, textAlign = TextAlign.Center)
        Text(details, modifier = Modifier.weight(1.4f).padding(horizontal = UiSpacing.cell), style = style, color = typeColor, textAlign = TextAlign.Center)
    }
}
