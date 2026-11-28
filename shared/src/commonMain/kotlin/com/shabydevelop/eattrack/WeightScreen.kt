package com.shabydevelop.eattrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.semantics.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch

@Composable
fun WeightScreen(clock: AppClock, load: suspend (Int, WeightEntry?, (WeightDataSource) -> Unit) -> Pair<WeightPage, WeightSummary?>, formatDateTime: (Long) -> String, kg: (Int) -> String, editKg: (Int) -> String, parseGrams: (String) -> Int?, dateTimeFields: @Composable (Long, (Long) -> Unit) -> Unit) {
    val scope = rememberCoroutineScope()
    var entries by remember { mutableStateOf<List<WeightEntry>>(emptyList()) }
    var period by androidx.compose.runtime.saveable.rememberSaveable { mutableIntStateOf(7) }
    var summary by remember { mutableStateOf<WeightSummary?>(null) }
    var more by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var add by remember { mutableStateOf(false) }
    var edit by remember { mutableStateOf<WeightEntry?>(null) }
    var timestamp by remember { mutableLongStateOf(0L) }
    var delete by remember { mutableStateOf<WeightEntry?>(null) }
    fun refresh(append: Boolean = false, action: (WeightDataSource) -> Unit = {}) {
        busy = true
        scope.launch {
            try {
                val cursor = if (append) entries.lastOrNull() else null
                val selectedPeriod = period
                val (page, totals) = load(selectedPeriod, cursor, action)
                entries = if (append) entries + page.entries else page.entries
                summary = totals
                more = page.hasMore
                error = false
            } catch (e: RepositoryFailure) { error = true }
            finally { busy = false }
        }
    }
    LaunchedEffect(Unit) { refresh() }
    val state = rememberLazyListState()
    LazyColumn(Modifier.fillMaxSize().lazyScrollIndicator(state), state = state, contentPadding = PaddingValues(UiSpacing.screen)) {
        item {
            AppOutlinedButton(onClick = { input = ""; edit = null; timestamp = clock.nowMillis(); add = true }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("דיווח משקל חדש") }
        }
        summary?.let { totals -> item {
            Spacer(Modifier.height(UiSpacing.normal))
            TableFrame {
                TableLine(listOf(2f,1f), Modifier.padding(vertical = UiSpacing.normal)) { i -> Text(if (i == 0) "משקל אחרון" else kg(totals.latest) + " ק״ג", style = UiTypography.tableCell) }
                HorizontalDivider()
                TableLine(listOf(2f,1f), Modifier.padding(vertical = UiSpacing.normal)) { i -> Text(if (i == 0) "שינוי מתחילת המעקב" else kg(totals.change) + " ק״ג", style = UiTypography.tableCell) }
            }
        } }
        item {
            Spacer(Modifier.height(UiSpacing.normal))
            SectionHeader("היסטוריית שקילות")
            Spacer(Modifier.height(UiSpacing.small))
            HistoryFilter("תקופה", listOf(7 to "7 ימים", 30 to "30 ימים", 0 to "הכל"), period, enabled = !busy) {
                period = it; entries = emptyList(); more = false; refresh()
            }
            Spacer(Modifier.height(UiSpacing.normal))
        }
        if (error) item { Text("לא ניתן להשלים את הפעולה", color = MaterialTheme.colorScheme.error); Spacer(Modifier.height(UiSpacing.normal)) }
        if (busy) item { CircularProgressIndicator(); Spacer(Modifier.height(UiSpacing.normal)) }
        if (!busy && !error && entries.isEmpty()) item { Text("אין שקילות בתקופה שנבחרה") }
        if (entries.isNotEmpty()) item {
            Row(Modifier.fillMaxWidth().tableSegment(top = true).tableHeaderBackground().padding(vertical = UiSpacing.normal).semantics { heading() }, verticalAlignment = Alignment.CenterVertically) {
                TableLine(listOf(2f,1f), Modifier.weight(1f)) { i -> TableHeaderText(if (i == 0) "תאריך ושעה" else "ק״ג") }
                TableHeaderText("פעולות", Modifier.width(UiSpacing.touch * 2), textAlign = TextAlign.Center)
            }
        }
        itemsIndexed(entries, key = { _, entry -> entry.id }) { index, entry ->
            Column(Modifier.fillMaxWidth().tableSegment(bottom = index == entries.lastIndex)) {
                val date = formatDateTime(entry.timestamp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TableLine(listOf(2f,1f), Modifier.weight(1f)) { i -> Text(if (i == 0) date else kg(entry.grams), style = UiTypography.tableCell) }
                    TableRowActions("שקילה $date", !busy, {
                        edit = entry
                        input = editKg(entry.grams)
                        timestamp = entry.timestamp
                        add = true
                    }, { delete = entry })
                }
                if (index != entries.lastIndex) HorizontalDivider()
            }
        }
        if (more && !busy && !error) item {
            Spacer(Modifier.height(UiSpacing.normal))
            AppOutlinedButton(onClick = { refresh(append = true) }, modifier = Modifier.fillMaxWidth()) { Text("טעינת שקילות נוספות") }
        }
        if (error && !busy) item {
            AppOutlinedButton(onClick = { refresh() }, modifier = Modifier.fillMaxWidth()) { Text("נסה שוב") }
        }
    }
    if (add) {
        val grams = parseGrams(input)
        AlertDialog(onDismissRequest = { add = false }, title = { Text(if (edit == null) "דיווח משקל חדש" else "עריכת שקילה", style = UiTypography.sectionTitle) },
            text = { Column(Modifier.visibleVerticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(UiSpacing.normal)) { OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("משקל בק״ג") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                if (edit != null) dateTimeFields(timestamp) { timestamp = it }
                if (edit != null && timestamp > clock.nowMillis()) Text("לא ניתן לבחור מועד עתידי", color = MaterialTheme.colorScheme.error)
            } },
            confirmButton = { AppTextButton(onClick = { grams?.let { value -> val existing = edit; val time = timestamp; refresh { repo -> if (existing == null) repo.add(value) else repo.update(existing.copy(grams = value, timestamp = time)) } }; add = false }, enabled = grams != null && grams > 0 && timestamp <= clock.nowMillis() && !busy) { Text("שמירה") } },
            dismissButton = { AppTextButton(onClick = { add = false }) { Text("ביטול") } })
    }
    delete?.let { entry ->
        AlertDialog(onDismissRequest = { delete = null }, title = { Text("מחיקת שקילה") }, text = { Text("למחוק את השקילה?") },
            confirmButton = { AppTextButton(onClick = { delete = null; refresh { it.delete(entry.id) } }, color = UiColors.unhealthy) { Text("מחיקה") } },
            dismissButton = { AppTextButton(onClick = { delete = null }) { Text("ביטול") } })
    }
}

